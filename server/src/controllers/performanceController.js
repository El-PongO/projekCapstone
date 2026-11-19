const {num,monthWib}=require('../utils/common');
const {Project,Commission}=require('../models');
module.exports={ get: async(req,res)=>{
 const [projects,commissions]=await Promise.all([
  Project.findAll({where:{agent_id:req.user.id},order:[['created_at','DESC']],limit:500}),
  Commission.findAll({where:{agent_id:req.user.id}})
 ]);
 const cMap=new Map(commissions.map(c=>[Number(c.project_id),c]));
 const sales=projects.map(p=>({projectCode:p.project_code,clientName:p.client_name,status:p.status,stage:p.execution_stage,dealValue:num(p.deal_value),date:p.created_at}));
 const income=projects.filter(p=>cMap.has(Number(p.id))).map(p=>{
  const c=cMap.get(Number(p.id));return {projectCode:p.project_code,clientName:p.client_name,commission:num(c.total_commission_amount),ratePercent:num(c.commission_rate_percent),date:c.created_at, payoutStatus:c.payout_status};
 });
 // Income uses full commissions owned by this agent, not just commissions on personally-owned projects (Upline may receive team share).
 const commissionProjects=await Project.findAll({where:{id:{[require('sequelize').Op.in]:commissions.map(c=>c.project_id)}}});
 const projectLookup=new Map(commissionProjects.map(p=>[Number(p.id),p]));
 const allIncome=commissions.map(c=>{
  const p=projectLookup.get(Number(c.project_id));const own=Number(p?.agent_id)===Number(req.user.id);
  return {projectCode:p?.project_code||'-',clientName:own?p?.client_name||'-':`Komisi Tim ${p?.project_code||''}`,commission:num(c.total_commission_amount),ratePercent:num(c.commission_rate_percent),date:p?.deal_date||c.created_at,payoutStatus:c.payout_status};
 });
 const dealProjects=projects.filter(p=>p.status==='deal');const month=monthWib();
 const monthlyCut=commissions.reduce((sum,c)=>{
  const p=projectLookup.get(Number(c.project_id));return p?.deal_date&&String(p.deal_date).slice(0,7)===month?sum+num(c.total_commission_amount):sum;
 },0);
 const totalIncome=allIncome.reduce((s,x)=>s+x.commission,0);
 res.json({stats:{totalSales:dealProjects.reduce((s,p)=>s+num(p.deal_value),0),totalIncome,totalDealsMade:dealProjects.length,monthlyCut},sales,income:allIncome});
} };
