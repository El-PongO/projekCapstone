const {Op,num}=require('../utils/common');
const {Project,User,Commission}=require('../models');
const {projectScope,children,publicProject}=require('../services/access');
module.exports={ get: async(req,res)=>{
 const where=await projectScope(req.user);
 const [totalProjects,dealCount,followUpCount,dealProjects,recent,commissionRows]=await Promise.all([
  Project.count({where}),Project.count({where:{...where,status:'deal'}}),Project.count({where:{...where,status:'follow_up'}}),
  Project.findAll({where:{...where,status:'deal'},attributes:['deal_value']}),
  Project.findAll({where,order:[['created_at','DESC']],limit:8}),
  Commission.findAll({where:{agent_id:req.user.id},attributes:['total_commission_amount']})
 ]);
 const agentCount=req.user.role==='admin'?await User.count({where:{role:{[Op.in]:['upline','downline']},is_active:true}}):req.user.role==='upline'?(await children(req.user.id)).length:0;
 const ids=[...new Set(recent.map(p=>Number(p.agent_id)))];const agents=await User.findAll({where:{id:{[Op.in]:ids}},attributes:['id','agent_code']});const names=new Map(agents.map(u=>[Number(u.id),u.agent_code]));
 res.json({role:req.user.role,stats:{totalProjects,dealCount,followUpCount,dealValue:dealProjects.reduce((s,x)=>s+num(x.deal_value),0),agentCount,myCommission:commissionRows.reduce((s,x)=>s+num(x.total_commission_amount),0)},recentProjects:recent.map(p=>{
  const out=publicProject(p,req.user,names.get(Number(p.agent_id)));
  return {projectCode:out.projectCode,title:out.clientName,status:out.status,stage:out.stage,dealValue:out.dealValue};
 })});
} };
