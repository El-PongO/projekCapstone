const {Router}=require('express'),db=require('../config/database');
const {Invoice,Payment,Project,Commission,CommissionPayout,User}=require('../models');
const {auth,roles}=require('../middleware/auth');
const {wrap,fail,id,nonempty,optional,amount,date,enumValue,page,code,num,round,Op,dateWib}=require('../utils/common');
const {projectScope,findProject}=require('../services/access');
const {notify,push}=require('../services/notify');
const invoices=Router(),payments=Router(),commissions=Router(),payouts=Router();
for(const r of [invoices,payments,commissions,payouts])r.use(auth);
async function getAllowedProjectIds(user){const list=await Project.findAll({where:await projectScope(user),attributes:['id']});return list.map(x=>x.id);}
// INVOICES
invoices.get('/',wrap(async(req,res)=>{
 const where=req.user.role==='admin'?{}:{project_id:{[Op.in]:await getAllowedProjectIds(req.user)}};
 res.json(await Invoice.findAll({where,order:[['invoice_date','DESC']],...page(req)}));
}));
invoices.post('/',roles('admin'),wrap(async(req,res)=>{
 const projectId=id(req.body.projectId),value=amount(req.body.totalAmount,true);
 const invoiceDate=date(req.body.invoiceDate||dateWib()),dueDate=date(req.body.dueDate);
 if(dueDate<invoiceDate)throw fail(400,'Jatuh tempo harus setelah tanggal invoice');
 const invoice=await db.transaction(async transaction=>{
  const p=await Project.findByPk(projectId,{transaction,lock:transaction.LOCK.UPDATE});
  if(!p||p.status!=='deal')throw fail(409,'Invoice hanya untuk proyek Deal');
  const prior=await Invoice.findAll({where:{project_id:projectId},transaction});
  const invoiced=prior.reduce((s,x)=>s+num(x.total_amount),0);
  if(round(invoiced+value)>round(num(p.deal_value)))throw fail(409,'Total invoice melebihi nilai Deal');
  return Invoice.create({invoice_number:nonempty(req.body.invoiceNumber||code('INV'),50),project_id:p.id,created_by:req.user.id,invoice_type:enumValue(req.body.invoiceType||'full',['dp','termin','final','full']),total_amount:value,invoice_date:invoiceDate,due_date:dueDate,status:'unpaid',notes:optional(req.body.notes,4000)},{transaction});
 });res.status(201).json(invoice);
}));
invoices.get('/:id',wrap(async(req,res)=>{
 const x=await Invoice.findByPk(id(req.params.id));if(!x)throw fail(404,'Invoice tidak ditemukan');await findProject(req.user,x.project_id);const rows=await Payment.findAll({where:{invoice_id:x.id},order:[['payment_date','ASC']]});res.json({invoice:x,payments:rows,paidAmount:round(rows.reduce((s,p)=>s+num(p.amount_paid),0))});
}));
// PAYMENTS = uang diterima dari klien (Admin saja membuat)
payments.get('/',wrap(async(req,res)=>{const where=req.user.role==='admin'?{}:{project_id:{[Op.in]:await getAllowedProjectIds(req.user)}};res.json(await Payment.findAll({where,order:[['payment_date','DESC']],...page(req)}));}));
payments.post('/',roles('admin'),wrap(async(req,res)=>{
 const invoiceId=id(req.body.invoiceId),value=amount(req.body.amountPaid,true),paymentDate=date(req.body.paymentDate||dateWib());
 const payment=await db.transaction(async transaction=>{
  const i=await Invoice.findByPk(invoiceId,{transaction,lock:transaction.LOCK.UPDATE});if(!i)throw fail(404,'Invoice tidak ditemukan');
  const previous=await Payment.findAll({where:{invoice_id:i.id},transaction});const paid=round(previous.reduce((s,p)=>s+num(p.amount_paid),0));
  if(round(paid+value)>round(num(i.total_amount)))throw fail(409,'Total pembayaran melebihi invoice');
  const status=round(paid+value)===round(num(i.total_amount))?'paid':'partial';
  const record=await Payment.create({project_id:i.project_id,invoice_id:i.id,payment_receipt_no:nonempty(req.body.receiptNumber||code('PAY'),50),amount_paid:value,payment_date:paymentDate,payment_method:nonempty(req.body.paymentMethod,50),payment_status:status,due_date:i.due_date},{transaction});
  await i.update({status},{transaction});return record;
 });res.status(201).json(payment);
}));
// COMMISSIONS = hak agen, Admin menentukan approval
commissions.get('/',wrap(async(req,res)=>{
 const where=req.user.role==='admin'?{}:{agent_id:req.user.id};
 if(req.query.status)where.payout_status=enumValue(req.query.status,['pending','approved','paid']);
 res.json(await Commission.findAll({where,order:[['created_at','DESC']],...page(req)}));
}));
commissions.get('/:id',wrap(async(req,res)=>{
 const c=await Commission.findByPk(id(req.params.id));if(!c||req.user.role!=='admin'&&Number(c.agent_id)!==Number(req.user.id))throw fail(404,'Komisi tidak ditemukan');
 const rows=await CommissionPayout.findAll({where:{commission_id:c.id}});
 res.json({commission:c,paidAmount:round(rows.reduce((s,p)=>s+num(p.amount_paid),0)),payouts:rows});
}));
commissions.post('/:id/approve',roles('admin'),wrap(async(req,res)=>{
 const result=await db.transaction(async transaction=>{
  const c=await Commission.findByPk(id(req.params.id),{transaction,lock:transaction.LOCK.UPDATE});
  if(!c)throw fail(404,'Komisi tidak ditemukan');
  if(c.payout_status!=='pending')throw fail(409,'Komisi sudah diproses');
  const project=await Project.findByPk(c.project_id,{transaction,lock:transaction.LOCK.UPDATE});
  if(!project||project.status!=='deal')throw fail(409,'Hanya komisi proyek Deal yang dapat disetujui');
  const bills=await Invoice.findAll({where:{project_id:c.project_id},transaction,lock:transaction.LOCK.UPDATE});
  if(!bills.length)throw fail(409,'Komisi belum dapat disetujui: invoice proyek belum tersedia');
  const receipt=await Payment.findAll({where:{invoice_id:{[Op.in]:bills.map(b=>b.id)}},transaction});
  const paid=new Map();for(const p of receipt)paid.set(Number(p.invoice_id),(paid.get(Number(p.invoice_id))||0)+num(p.amount_paid));
  // Kebijakan konservatif: semua invoice proyek harus lunas sebelum komisi approved.
  if(bills.some(b=>b.status!=='paid'||round(paid.get(Number(b.id))||0)<round(num(b.total_amount))))
   throw fail(409,'Komisi menunggu pelunasan dan verifikasi seluruh invoice klien');
  await c.update({payout_status:'approved'},{transaction});
  await notify(c.agent_id,'Komisi Disetujui',`Komisi proyek ${c.project_id} disetujui`,'commission',transaction);
  return {commission:c,agentId:c.agent_id};
 });
 await push(result.agentId,'Komisi Disetujui','Komisi Anda sudah disetujui');
 res.json(result.commission);
}));
// COMMISSION PAYOUTS = uang keluar untuk agen
payouts.get('/',roles('admin'),wrap(async(req,res)=>res.json(await CommissionPayout.findAll({order:[['payout_date','DESC']],...page(req)}))));
payouts.post('/',roles('admin'),wrap(async(req,res)=>{
 const commissionId=id(req.body.commissionId),value=amount(req.body.amountPaid,true),payoutDate=date(req.body.payoutDate||dateWib());
 const outcome=await db.transaction(async transaction=>{
  const c=await Commission.findByPk(commissionId,{transaction,lock:transaction.LOCK.UPDATE});
  if(!c)throw fail(404,'Komisi tidak ditemukan');if(c.payout_status!=='approved')throw fail(409,'Komisi harus Approved dan belum Paid');
  const previous=await CommissionPayout.findAll({where:{commission_id:c.id},transaction});const paid=round(previous.reduce((s,p)=>s+num(p.amount_paid),0));
  if(round(paid+value)>round(num(c.total_commission_amount)))throw fail(409,'Pencairan melebihi komisi disetujui');
  const pay=await CommissionPayout.create({commission_id:c.id,payout_number:nonempty(req.body.payoutNumber||code('CPO'),50),amount_paid:value,payout_date:payoutDate,payment_method:nonempty(req.body.paymentMethod,50),reference_number:optional(req.body.referenceNumber,100),recorded_by:req.user.id,notes:optional(req.body.notes,4000)},{transaction});
  if(round(paid+value)===round(num(c.total_commission_amount)))await c.update({payout_status:'paid',payout_date:new Date()},{transaction});
  await notify(c.agent_id,'Pencairan Komisi',`Komisi sebesar ${value} dicairkan`,'commission',transaction);
  return {payout:pay,agentId:c.agent_id};
 });await push(outcome.agentId,'Pencairan Komisi',`Pencairan komisi ${value} tercatat`);
 res.status(201).json(outcome.payout);
}));
module.exports={invoices,payments,commissions,payouts};
