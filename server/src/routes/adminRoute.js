const r=require('express').Router();const {auth,roles}=require('../middleware/auth');
const {User,Project,Invoice,Payment,ProjectApproval,Commission,AgentHierarchy}=require('../models');
const {wrap,Op,num,round}=require('../utils/common');
r.use(auth,roles('admin'));
r.get('/overview',wrap(async(req,res)=>{
 const [uplines,downlines,projects,deals,pendingApprovals,unpaidInvoices,commissions,team]=await Promise.all([
  User.count({where:{role:'upline',is_active:true}}),User.count({where:{role:'downline',is_active:true}}),
  Project.count(),Project.findAll({where:{status:'deal'},attributes:['deal_value']}),
  ProjectApproval.count({where:{status:'pending'}}),Invoice.findAll({where:{status:{[Op.ne]:'paid'}}}),
  Commission.findAll({attributes:['total_commission_amount','payout_status']}),AgentHierarchy.count()
 ]);
 let outstanding=0;
 for(const invoice of unpaidInvoices){const rows=await Payment.findAll({where:{invoice_id:invoice.id},attributes:['amount_paid']});outstanding+=Math.max(0,num(invoice.total_amount)-rows.reduce((s,p)=>s+num(p.amount_paid),0));}
 res.json({agents:{uplines,downlines,relations:team},projects:{total:projects,deals:deals.length,dealValue:round(deals.reduce((s,p)=>s+num(p.deal_value),0))},approvals:{pending:pendingApprovals},invoices:{open:unpaidInvoices.length,outstanding:round(outstanding)},commissions:{total:round(commissions.reduce((s,c)=>s+num(c.total_commission_amount),0)),pending:commissions.filter(c=>c.payout_status==='pending').length}});
}));
module.exports=r;
