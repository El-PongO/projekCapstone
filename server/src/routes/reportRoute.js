'use strict';
const r = require('express').Router();
const db = require('../config/database');
const { QueryTypes } = require('sequelize');
const ExcelJS = require('exceljs');
const PDFDocument = require('pdfkit');
const { auth, roles } = require('../middleware/auth');
const { wrap, fail, date, dateWib } = require('../utils/common');
r.use(auth, roles('admin'));
// Semua rentang tanggal di-parameterize untuk mencegah SQL injection.
// Filter mengikuti tanggal transaksi (bukan tanggal ekspor).
const sql = {
  'agent-performance': `SELECT u.agent_code AS agent,u.role,
     COUNT(p.id) AS total_projects,
     SUM(CASE WHEN p.status='deal' THEN 1 ELSE 0 END) AS deals,
     ROUND(100*SUM(CASE WHEN p.status='deal' THEN 1 ELSE 0 END)/NULLIF(COUNT(p.id),0),2) AS conversion_percent,
     COALESCE(SUM(CASE WHEN p.status='deal' THEN p.deal_value ELSE 0 END),0) AS deal_value
     FROM users u LEFT JOIN projects p ON p.agent_id=u.id AND DATE(p.created_at) BETWEEN :from AND :to
     WHERE u.role IN ('upline','downline') GROUP BY u.id,u.agent_code,u.role ORDER BY deal_value DESC`,
  'commission-sharing': `SELECT u.agent_code AS agent,u.agent_type,u.rate_value_percent,
     COUNT(c.id) AS commission_count,COALESCE(SUM(c.total_commission_amount),0) AS total_commission,
     SUM(CASE WHEN c.payout_status='paid' THEN 1 ELSE 0 END) AS fully_paid_count,
     COALESCE(SUM(CASE WHEN c.payout_status='pending' THEN c.total_commission_amount ELSE 0 END),0) AS pending_amount
     FROM users u LEFT JOIN commissions c ON c.agent_id=u.id AND DATE(c.created_at) BETWEEN :from AND :to
     WHERE u.role IN ('upline','downline') GROUP BY u.id,u.agent_code,u.agent_type,u.rate_value_percent ORDER BY total_commission DESC`,
  'project-conversion': `SELECT u.agent_code AS agent,COUNT(p.id) AS total_projects,
     SUM(CASE WHEN p.status='follow_up' THEN 1 ELSE 0 END) AS follow_up,
     SUM(CASE WHEN p.status='deal' THEN 1 ELSE 0 END) AS deals,
     SUM(CASE WHEN p.status='no_deal' THEN 1 ELSE 0 END) AS no_deal,
     SUM(CASE WHEN p.status='cancelled' THEN 1 ELSE 0 END) AS cancelled,
     ROUND(100*SUM(CASE WHEN p.status='deal' THEN 1 ELSE 0 END)/NULLIF(COUNT(p.id),0),2) AS conversion_percent
     FROM users u LEFT JOIN projects p ON p.agent_id=u.id AND DATE(p.created_at) BETWEEN :from AND :to
     WHERE u.role IN ('upline','downline') GROUP BY u.id,u.agent_code ORDER BY conversion_percent DESC`,
  'deal-summary': `SELECT DATE_FORMAT(deal_date,'%Y-%m') AS month,
     COUNT(*) AS deals,COALESCE(SUM(deal_value),0) AS deal_value
     FROM projects WHERE status='deal' AND deal_date BETWEEN :from AND :to GROUP BY month ORDER BY month DESC`,
  'payment-history': `SELECT py.payment_receipt_no AS receipt,i.invoice_number AS invoice,
     p.project_code AS project,py.amount_paid,py.payment_date,py.payment_method
     FROM payments py LEFT JOIN invoices i ON i.id=py.invoice_id
     JOIN projects p ON p.id=py.project_id
     WHERE py.payment_date BETWEEN :from AND :to ORDER BY py.payment_date DESC`,
  'aging-payment': `SELECT i.invoice_number AS invoice,p.project_code AS project,i.due_date,
     i.total_amount,COALESCE(pay.paid_amount,0) AS paid_amount,
     GREATEST(0,i.total_amount-COALESCE(pay.paid_amount,0)) AS outstanding,
     GREATEST(0,DATEDIFF(:asOf,i.due_date)) AS overdue_days,
     CASE WHEN i.due_date>=:asOf THEN 'Belum jatuh tempo'
          WHEN DATEDIFF(:asOf,i.due_date)<=30 THEN '1-30 hari'
          WHEN DATEDIFF(:asOf,i.due_date)<=60 THEN '31-60 hari'
          ELSE '>60 hari' END AS aging_bucket
     FROM invoices i JOIN projects p ON p.id=i.project_id
     LEFT JOIN (SELECT invoice_id,SUM(amount_paid) AS paid_amount FROM payments GROUP BY invoice_id) pay ON pay.invoice_id=i.id
     WHERE i.due_date BETWEEN :from AND :to
       AND i.total_amount>COALESCE(pay.paid_amount,0)
     ORDER BY overdue_days DESC`,
  'cancel-analysis': `SELECT status,COALESCE(cancel_reason,'Tidak ada alasan') AS reason,COUNT(*) AS total
     FROM projects WHERE status IN ('no_deal','cancelled')
       AND DATE(updated_at) BETWEEN :from AND :to
     GROUP BY status,reason ORDER BY total DESC`,
  'agent-hierarchy': `SELECT up.agent_code AS upline,downu.agent_code AS downline,
     h.created_at AS joined_at,COUNT(DISTINCT p.id) AS projects,
     COUNT(DISTINCT CASE WHEN p.status='deal' THEN p.id END) AS deals,
     COALESCE(SUM(CASE WHEN p.status='deal' THEN p.deal_value ELSE 0 END),0) AS downline_deal_value,
     COALESCE(comm.total_commission,0) AS downline_commission
     FROM agent_hierarchies h JOIN users up ON up.id=h.upline_id
     JOIN users downu ON downu.id=h.downline_id
     LEFT JOIN projects p ON p.agent_id=downu.id AND DATE(p.created_at) BETWEEN :from AND :to
     LEFT JOIN (SELECT agent_id,SUM(total_commission_amount) AS total_commission
       FROM commissions WHERE DATE(created_at) BETWEEN :from AND :to GROUP BY agent_id) comm ON comm.agent_id=downu.id
     WHERE h.depth=1
     GROUP BY h.id,up.agent_code,downu.agent_code,h.created_at,comm.total_commission
     ORDER BY up.agent_code,downu.agent_code`,
  'project-category': `SELECT c.category_name AS category,
     COUNT(DISTINCT p.id) AS project_count,
     COALESCE(SUM(CASE WHEN p.id IS NOT NULL THEN pi.quantity*pi.price ELSE 0 END),0) AS line_value
     FROM categories c LEFT JOIN items it ON it.category_id=c.id
     LEFT JOIN project_items pi ON pi.item_id=it.id
     LEFT JOIN projects p ON p.id=pi.project_id AND DATE(p.created_at) BETWEEN :from AND :to
     GROUP BY c.id,c.category_name ORDER BY line_value DESC`,
  'revenue-forecast': `SELECT COALESCE(SUM(CASE WHEN status='follow_up' THEN deal_value ELSE 0 END),0) AS active_pipeline,
     COUNT(CASE WHEN status='follow_up' THEN 1 END) AS active_count,
     COUNT(CASE WHEN status='deal' THEN 1 END) AS historic_deals,
     COUNT(*) AS total_projects FROM projects WHERE DATE(created_at) BETWEEN :from AND :to`
};
r.get('/types', (_req, res) => res.json(Object.keys(sql)));
r.get('/:type', wrap(async (req, res) => {
  const type = req.params.type;
  if (!Object.hasOwn(sql,type)) throw fail(404,'Jenis report tidak tersedia');
  const format = req.query.format || 'json';
  if (!['json','excel','pdf'].includes(format)) throw fail(400,'format harus json, excel, atau pdf');
  const from = req.query.from ? date(req.query.from) : '1900-01-01';
  const to = req.query.to ? date(req.query.to) : '9999-12-31';
  if (from > to) throw fail(400,'from tidak boleh setelah to');
  let data = await db.query(sql[type], { replacements: { from,to,asOf:dateWib() }, type: QueryTypes.SELECT });
  if (type === 'revenue-forecast') {
    const x = data[0] || {}, pipeline = Number(x.active_pipeline||0);
    const all = Number(x.total_projects||0), deals = Number(x.historic_deals||0);
    const rate = all ? deals / all : 0;
    data = [{ active_pipeline: pipeline, active_count: Number(x.active_count||0),
      historical_conversion_rate_percent: Math.round(rate*10000)/100,
      estimated_next_month_value: Math.round(pipeline*rate*100)/100,
      note: 'Estimasi ilustratif memakai conversion rate historis, bukan prediksi resmi' }];
  }
  if (format === 'json') return res.json({ type, period:{from,to}, count:data.length, data });
  const keys = data.length ? Object.keys(data[0]) : ['empty'];
  if (format === 'excel') {
    const wb = new ExcelJS.Workbook(), ws = wb.addWorksheet(type.slice(0,31));
    ws.addRow(keys);
    for (const x of data) ws.addRow(keys.map(k => {
      const v = x[k] ?? '';
      return typeof v === 'string' && /^[=+@-]/.test(v) ? "'"+v : v;
    }));
    ws.getRow(1).font = {bold:true}; ws.columns = keys.map(()=>({width:25}));
    const buffer = await wb.xlsx.writeBuffer();
    res.set('Content-Type','application/vnd.openxmlformats-officedocument.spreadsheetml.sheet');
    res.set('Content-Disposition',`attachment; filename="${type}.xlsx"`);
    return res.send(Buffer.from(buffer));
  }
  res.set('Content-Type','application/pdf');
  res.set('Content-Disposition',`attachment; filename="${type}.pdf"`);
  const pdf = new PDFDocument({size:'A4',margin:35}); pdf.pipe(res);
  pdf.fontSize(16).text('INTEGRA AGENCY - '+type).moveDown();
  pdf.fontSize(9).text(`Periode: ${from} - ${to}`).moveDown();
  for(const x of data) pdf.fontSize(9).text(keys.map(k=>`${k}: ${x[k]??'-'}`).join(' | '),{width:520}).moveDown(0.4);
  if(!data.length) pdf.text('Belum ada data');
  pdf.end();
}));
module.exports = r;
