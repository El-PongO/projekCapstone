const r=require('express').Router(),db=require('../config/database');
const {QueryTypes}=require('sequelize');const ExcelJS=require('exceljs');const PDFDocument=require('pdfkit');
const {auth,roles}=require('../middleware/auth');const {wrap,fail,enumValue}=require('../utils/common');
r.use(auth,roles('admin'));
// Laporan SQL berbasis tabel aktual, tanpa tabel tambahan.
const sql={
 'agent-performance':`SELECT u.agent_code AS agent, u.role, COUNT(p.id) AS total_projects, SUM(CASE WHEN p.status='deal' THEN 1 ELSE 0 END) AS deals, COALESCE(SUM(CASE WHEN p.status='deal' THEN p.deal_value ELSE 0 END),0) AS deal_value FROM users u LEFT JOIN projects p ON p.agent_id=u.id WHERE u.role IN ('upline','downline') GROUP BY u.id,u.agent_code,u.role ORDER BY deals DESC`,
 'commission-sharing':`SELECT u.agent_code AS agent, COUNT(c.id) AS commission_count, COALESCE(SUM(c.total_commission_amount),0) AS total_commission, SUM(CASE WHEN c.payout_status='paid' THEN 1 ELSE 0 END) AS fully_paid_count FROM users u LEFT JOIN commissions c ON c.agent_id=u.id WHERE u.role IN ('upline','downline') GROUP BY u.id,u.agent_code ORDER BY total_commission DESC`,
 'project-conversion':`SELECT u.agent_code AS agent, COUNT(p.id) AS total_projects, SUM(CASE WHEN p.status='deal' THEN 1 ELSE 0 END) AS deals, ROUND(100*SUM(CASE WHEN p.status='deal' THEN 1 ELSE 0 END)/NULLIF(COUNT(p.id),0),2) AS conversion_percent FROM users u LEFT JOIN projects p ON p.agent_id=u.id WHERE u.role IN ('upline','downline') GROUP BY u.id,u.agent_code ORDER BY conversion_percent DESC`,
 'deal-summary':`SELECT DATE_FORMAT(COALESCE(deal_date,DATE(updated_at)),'%Y-%m') AS month,COUNT(*) AS deals,COALESCE(SUM(deal_value),0) AS deal_value FROM projects WHERE status='deal' GROUP BY month ORDER BY month DESC`,
 'payment-history':`SELECT py.payment_receipt_no AS receipt, i.invoice_number AS invoice, p.project_code AS project, py.amount_paid,py.payment_date,py.payment_method FROM payments py LEFT JOIN invoices i ON i.id=py.invoice_id JOIN projects p ON p.id=py.project_id ORDER BY py.payment_date DESC`,
 'aging-payment':`SELECT i.invoice_number AS invoice,p.project_code AS project,i.due_date,i.total_amount,COALESCE((SELECT SUM(y.amount_paid) FROM payments y WHERE y.invoice_id=i.id),0) AS paid_amount,GREATEST(0,i.total_amount-COALESCE((SELECT SUM(y.amount_paid) FROM payments y WHERE y.invoice_id=i.id),0)) AS outstanding, GREATEST(0,DATEDIFF(CURDATE(),i.due_date)) AS overdue_days FROM invoices i JOIN projects p ON p.id=i.project_id WHERE i.status<>'paid' ORDER BY overdue_days DESC`,
 'cancel-analysis':`SELECT status,COALESCE(cancel_reason,'Tidak ada alasan') AS reason,COUNT(*) AS total FROM projects WHERE status IN ('no_deal','cancelled') GROUP BY status,reason ORDER BY total DESC`,
 'agent-hierarchy':`SELECT up.agent_code AS upline,downu.agent_code AS downline,h.created_at AS joined_at FROM agent_hierarchies h JOIN users up ON up.id=h.upline_id JOIN users downu ON downu.id=h.downline_id WHERE h.depth=1 ORDER BY up.agent_code,downu.agent_code`,
 'project-category':`SELECT c.category_name AS category,COUNT(DISTINCT pi.project_id) AS project_count,COALESCE(SUM(pi.quantity*pi.price),0) AS line_value FROM categories c LEFT JOIN items it ON it.category_id=c.id LEFT JOIN project_items pi ON pi.item_id=it.id GROUP BY c.id,c.category_name ORDER BY project_count DESC`,
 'revenue-forecast':`SELECT COALESCE(SUM(CASE WHEN status='follow_up' THEN deal_value ELSE 0 END),0) AS active_pipeline,COUNT(CASE WHEN status='follow_up' THEN 1 END) AS active_count,COUNT(CASE WHEN status='deal' THEN 1 END) AS historic_deals,COUNT(*) AS total_projects FROM projects`
};
r.get('/types',(req,res)=>res.json(Object.keys(sql)));
r.get('/:type',wrap(async(req,res)=>{
 const type=req.params.type;if(!Object.hasOwn(sql,type))throw fail(404,'Jenis report tidak tersedia');
 const format=req.query.format||'json';if(!['json','excel','pdf'].includes(format))throw fail(400,'format harus json, excel, atau pdf');
 let data=await db.query(sql[type],{type:QueryTypes.SELECT});
 if(type==='revenue-forecast'){
  const x=data[0]||{},pipeline=Number(x.active_pipeline||0),all=Number(x.total_projects||0),deals=Number(x.historic_deals||0);
  const rate=all?deals/all:0;
  data=[{active_pipeline:pipeline,historical_conversion_rate_percent:Math.round(rate*10000)/100,estimated_next_month_value:Math.round(pipeline*rate*100)/100,note:'Estimasi ilustratif memakai conversion rate historis, bukan prediksi resmi'}];
 }
 if(format==='json')return res.json({type,count:data.length,data});
 const keys=data.length?Object.keys(data[0]):['empty'];
 if(format==='excel'){
  const wb=new ExcelJS.Workbook(),ws=wb.addWorksheet(type.slice(0,31));ws.addRow(keys);for(const x of data)ws.addRow(keys.map(k=>{const v=x[k]??'';return typeof v==='string'&&/^[=+@-]/.test(v)?'\''+v:v;}));ws.getRow(1).font={bold:true};ws.columns=keys.map(()=>({width:25}));
  const buffer=await wb.xlsx.writeBuffer();res.set('Content-Type','application/vnd.openxmlformats-officedocument.spreadsheetml.sheet');res.set('Content-Disposition',`attachment; filename="${type}.xlsx"`);return res.send(Buffer.from(buffer));
 }
 res.set('Content-Type','application/pdf');res.set('Content-Disposition',`attachment; filename="${type}.pdf"`);
 const pdf=new PDFDocument({size:'A4',margin:35});pdf.pipe(res);pdf.fontSize(16).text('INTEGRA AGENCY - '+type).moveDown();
 for(const x of data){pdf.fontSize(9).text(keys.map(k=>`${k}: ${x[k]??'-'}`).join(' | '),{width:520}).moveDown(0.4);}
 if(!data.length)pdf.text('Belum ada data');pdf.end();
}));
module.exports=r;
