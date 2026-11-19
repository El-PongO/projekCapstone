const r=require('express').Router();const {auth,roles}=require('../middleware/auth');
const {wrap,fail,id,num,Op,monthWib}=require('../utils/common');
const {User,Project,AgentHierarchy,Commission,ProjectActivityLog}=require('../models');
const {children,publicProject}=require('../services/access');
r.use(auth,roles('upline'));
async function teamData(userId){const ids=await children(userId);const [agents,rels,projects]=await Promise.all([
 User.findAll({where:{id:{[Op.in]:ids},is_active:true},attributes:['id','agent_code']}),
 AgentHierarchy.findAll({where:{upline_id:userId,depth:1}}),
 Project.findAll({where:{agent_id:{[Op.in]:ids}},order:[['created_at','DESC']]})]);
 const joined=new Map(rels.map(x=>[Number(x.downline_id),x.created_at]));
 const list=agents.map(agent=>{
  const own=projects.filter(p=>Number(p.agent_id)===Number(agent.id));
  const deals=own.filter(p=>p.status==='deal').length;
  const start=new Date(joined.get(Number(agent.id))||new Date());const end=new Date();const months=Math.max(1,(end.getFullYear()-start.getFullYear())*12+end.getMonth()-start.getMonth()+1);
  return {id:Number(agent.id),agentCode:agent.agent_code,joinedAt:joined.get(Number(agent.id)),totalDeals:deals,dealsPerMonth:Math.round(deals/months*10)/10};
 });return {list,projects,agents};
}
r.get('/downlines',wrap(async(req,res)=>{
 const {list}=await teamData(req.user.id);res.json({summary:{downlineCount:list.length,totalDeals:list.reduce((s,a)=>s+a.totalDeals,0)},downlines:list});
}));
r.get('/downlines/:id',wrap(async(req,res)=>{
 const {list,projects}=await teamData(req.user.id);const d=list.find(x=>x.id===id(req.params.id));if(!d)throw fail(404,'Downline tidak ditemukan');
 res.json({downline:d,projects:projects.filter(p=>Number(p.agent_id)===d.id).map(p=>({id:Number(p.id),code:p.project_code,status:p.status,stage:p.execution_stage,dealValue:num(p.deal_value),updatedAt:p.updated_at}))});
}));
r.get('/crm',wrap(async(req,res)=>{
 const ids=await children(req.user.id);const allowed=[req.user.id,...ids];
 const [rows,agents]=await Promise.all([
  Project.findAll({where:{agent_id:{[Op.in]:allowed}},order:[['updated_at','DESC']],limit:500}),
  User.findAll({where:{id:{[Op.in]:allowed}},attributes:['id','agent_code']})
 ]);
 const codes=new Map(agents.map(x=>[Number(x.id),x.agent_code]));
 const projects=rows.map(p=>{
  const safe=publicProject(p,req.user,codes.get(Number(p.agent_id)));
  return {id:safe.id,projectCode:safe.projectCode,clientName:safe.clientName,agentCode:safe.isAnonymized?codes.get(Number(p.agent_id)):'My Project',status:safe.status,stage:safe.stage,dealValue:safe.dealValue,cancelReason:safe.cancelReason||'',createdAt:safe.createdAt,updatedAt:safe.updatedAt};
 });
 const active=rows.filter(p=>p.status==='follow_up');
 const clientKeys=new Set(rows.map(p=>p.client_id?`id-${p.client_id}`:p.client_name.trim().toLowerCase()));
 res.json({summary:{totalPotentialValue:active.reduce((s,p)=>s+num(p.deal_value),0),numberOfClients:clientKeys.size,activeCount:active.length,dealCount:rows.filter(p=>p.status==='deal').length,droppedCount:rows.filter(p=>['no_deal','cancelled'].includes(p.status)).length},projects});
}));
r.get('/dashboard',wrap(async(req,res)=>{
 const ids=await children(req.user.id);const p=await Project.findAll({where:{agent_id:{[Op.in]:[req.user.id,...ids]}}});
 const mine=await Project.findAll({where:{agent_id:req.user.id}});
 const c=await Commission.findAll({where:{agent_id:req.user.id}});
 const month=monthWib();const cut=c.filter(x=>{const project=p.find(y=>Number(y.id)===Number(x.project_id));return project?.deal_date&&String(project.deal_date).slice(0,7)===month;}).reduce((s,x)=>s+num(x.total_commission_amount),0);
 const activities=await ProjectActivityLog.findAll({where:{agent_id:{[Op.in]:ids}},order:[['created_at','DESC']],limit:10});
 const agents=await User.findAll({where:{id:{[Op.in]:ids}},attributes:['id','agent_code']});const codes=new Map(agents.map(x=>[Number(x.id),x.agent_code]));
 res.json({monthlyCut:cut,projectsHeld:p.length,downlineCount:ids.length,activeDeals:mine.filter(x=>x.status==='follow_up').map(x=>({id:Number(x.id),clientName:x.client_name,amount:num(x.deal_value)})),activities:activities.map(a=>({agentCode:codes.get(Number(a.agent_id))||'-',description:'Aktivitas proyek Downline diperbarui',time:a.created_at}))});
}));
module.exports=r;
