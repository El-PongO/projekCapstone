'use strict';
const r = require('express').Router();
const db = require('../config/database');
const { QueryTypes } = require('sequelize');
const ExcelJS = require('exceljs');
const PDFDocument = require('pdfkit');
const { auth, roles } = require('../middleware/auth');
const { wrap, fail, date, dateWib } = require('../utils/common');
const {reports, columns, label, addTableSheet, drawPdfReport} = require('../services/reportPresentation');
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
  'commission-sharing': `SELECT p.project_code AS project,p.deal_date,p.deal_value,
     u.agent_code AS recipient,c.agent_type,c.commission_rate_percent AS rate_percent,
     c.total_commission_amount AS share_amount,c.payout_status
     FROM projects p JOIN commissions c ON c.project_id=p.id
     JOIN users u ON u.id=c.agent_id
     WHERE p.status='deal' AND p.deal_date BETWEEN :from AND :to
     UNION ALL
     SELECT p.project_code,p.deal_date,p.deal_value,'PT' AS recipient,'company' AS agent_type,
     NULL AS rate_percent,p.deal_value-COALESCE(comm.total_commission,0) AS share_amount,
     NULL AS payout_status
     FROM projects p LEFT JOIN (SELECT project_id,SUM(total_commission_amount) AS total_commission
       FROM commissions GROUP BY project_id) comm ON comm.project_id=p.id
     WHERE p.status='deal' AND p.deal_date BETWEEN :from AND :to
     ORDER BY deal_date DESC,project,recipient`,
  'project-conversion': `SELECT u.agent_code AS agent,COUNT(p.id) AS total_projects,
     SUM(CASE WHEN p.status='follow_up' THEN 1 ELSE 0 END) AS follow_up,
     SUM(CASE WHEN p.status='deal' THEN 1 ELSE 0 END) AS deals,
     SUM(CASE WHEN p.status='no_deal' THEN 1 ELSE 0 END) AS no_deal,
     SUM(CASE WHEN p.status='cancelled' THEN 1 ELSE 0 END) AS cancelled,
     ROUND(100*SUM(CASE WHEN p.status='deal' THEN 1 ELSE 0 END)/NULLIF(COUNT(p.id),0),2) AS conversion_percent
     FROM users u LEFT JOIN projects p ON p.agent_id=u.id AND DATE(p.created_at) BETWEEN :from AND :to
     WHERE u.role IN ('upline','downline') GROUP BY u.id,u.agent_code ORDER BY conversion_percent DESC`,
  'deal-summary': `SELECT p.project_code AS project,p.client_name AS client,u.agent_code AS agent,
     p.deal_date,p.deal_value
     FROM projects p JOIN users u ON u.id=p.agent_id
     WHERE p.status='deal' AND p.deal_date BETWEEN :from AND :to ORDER BY p.deal_date DESC,p.id DESC`,
  'payment-history': `SELECT py.payment_receipt_no AS receipt,i.invoice_number AS invoice,
     p.project_code AS project,py.amount_paid,py.payment_date,py.payment_method
     FROM payments py LEFT JOIN invoices i ON i.id=py.invoice_id
     JOIN projects p ON p.id=py.project_id
     WHERE p.status='deal' AND py.payment_date BETWEEN :from AND :to ORDER BY py.payment_date DESC`,
  'aging-payment': `SELECT i.invoice_number AS invoice,p.project_code AS project,i.due_date,
     i.total_amount,COALESCE(pay.paid_amount,0) AS paid_amount,
     GREATEST(0,i.total_amount-COALESCE(pay.paid_amount,0)) AS outstanding,
     GREATEST(0,DATEDIFF(:asOf,i.due_date)) AS overdue_days,
     CASE WHEN i.due_date>=:asOf THEN 'Belum jatuh tempo'
          WHEN DATEDIFF(:asOf,i.due_date)<=30 THEN '1-30 hari'
          WHEN DATEDIFF(:asOf,i.due_date)<=60 THEN '31-60 hari'
          ELSE '>60 hari' END AS aging_bucket
     FROM invoices i JOIN projects p ON p.id=i.project_id
     LEFT JOIN (SELECT invoice_id,SUM(amount_paid) AS paid_amount FROM payments
       WHERE payment_date<=:asOf GROUP BY invoice_id) pay ON pay.invoice_id=i.id
     WHERE p.status='deal' AND i.invoice_date<=:asOf AND i.due_date BETWEEN :from AND :to
       AND i.total_amount>COALESCE(pay.paid_amount,0)
     ORDER BY overdue_days DESC`,
  'cancel-analysis': `SELECT p.project_code AS project,p.client_name AS client,u.agent_code AS agent,
     p.status,COALESCE(NULLIF(p.cancel_reason,''),'Tidak ada alasan') AS reason,p.updated_at
     FROM projects p JOIN users u ON u.id=p.agent_id WHERE p.status IN ('no_deal','cancelled')
       AND DATE(p.updated_at) BETWEEN :from AND :to ORDER BY p.updated_at DESC,p.id DESC`,
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
     COUNT(DISTINCT CASE WHEN p.status='deal' THEN p.id END) AS deal_count,
     COALESCE(SUM(CASE WHEN p.status='deal' THEN pi.quantity*pi.price ELSE 0 END),0) AS line_value
     FROM categories c LEFT JOIN items it ON it.category_id=c.id
     LEFT JOIN project_items pi ON pi.item_id=it.id
     LEFT JOIN projects p ON p.id=pi.project_id AND DATE(p.created_at) BETWEEN :from AND :to
     GROUP BY c.id,c.category_name ORDER BY line_value DESC`,
  'revenue-forecast': `SELECT p.project_code AS project,p.client_name AS client,u.agent_code AS agent,
     p.execution_stage AS stage,p.deal_value AS potential_revenue,
     stats.historic_deals,stats.total_projects
     FROM projects p JOIN users u ON u.id=p.agent_id
     CROSS JOIN (SELECT COUNT(CASE WHEN status='deal' THEN 1 END) AS historic_deals,
       COUNT(*) AS total_projects FROM projects WHERE DATE(created_at) BETWEEN :from AND :to) stats
     WHERE p.status='follow_up' AND DATE(p.created_at) BETWEEN :from AND :to
     ORDER BY p.deal_value DESC,p.id DESC`
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
    data = data.map(({historic_deals,total_projects,...project}) => {
      const rate = Number(total_projects) ? Number(historic_deals) / Number(total_projects) : 0;
      return {...project, historical_conversion_rate_percent: Math.round(rate*10000)/100,
        estimated_revenue: Math.round(Number(project.potential_revenue)*rate*100)/100,
        note: 'Estimasi ilustratif, tanpa tanggal realisasi'};
    });
  }
  const total = key => Math.round(data.reduce((sum,row) => sum + Number(row[key] || 0),0)*100)/100;
  const summary = type === 'deal-summary' ? {deals:data.length,deal_value:total('deal_value')}
    : type === 'revenue-forecast' ? {active_projects:data.length,potential_revenue:total('potential_revenue'),estimated_revenue:total('estimated_revenue')}
    : type === 'aging-payment' ? {invoices:data.length,outstanding:total('outstanding')}
    : type === 'payment-history' ? {payments:data.length,amount_paid:total('amount_paid')}
    : {};
  const reportColumns = columns(type);
  if (format === 'json') return res.json({ type, title:reports[type][0], period:{from,to}, count:data.length, columns:reportColumns, summary, data });
  if (format === 'excel') {
    const wb = new ExcelJS.Workbook();
    addTableSheet(wb, type.slice(0,31), reportColumns.map(c=>c.label),
      data.map(row=>reportColumns.map(c=>row[c.key])));
    if (Object.keys(summary).length) {
      addTableSheet(wb, 'Ringkasan', ['Keterangan','Nilai'],
        Object.entries(summary).map(([key,value])=>[label(key),value]));
    }
    const buffer = await wb.xlsx.writeBuffer();
    res.set('Content-Type','application/vnd.openxmlformats-officedocument.spreadsheetml.sheet');
    res.set('Content-Disposition',`attachment; filename="${type}.xlsx"`);
    return res.send(Buffer.from(buffer));
  }
  res.set('Content-Type','application/pdf');
  res.set('Content-Disposition',`attachment; filename="${type}.pdf"`);
  const pdf = new PDFDocument({size:'A4',layout:'landscape',margin:35}); pdf.pipe(res);
  drawPdfReport(pdf,type,from,to,data,summary);
  pdf.end();
}));
module.exports = r;
