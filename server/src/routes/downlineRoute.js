const r=require('express').Router();const {auth,roles}=require('../middleware/auth');
const {Project,Commission,FollowUpTask,AgentHierarchy,User}=require('../models');
const {wrap,num,round}=require('../utils/common');
r.use(auth,roles('downline'));
r.get('/dashboard',wrap(async(req,res)=>{
 const [projects,commissions,tasks,relation]=await Promise.all([
  Project.findAll({where:{agent_id:req.user.id},order:[['created_at','DESC']]}),
  Commission.findAll({where:{agent_id:req.user.id}}),
  FollowUpTask.findAll({where:{agent_id:req.user.id,status:'pending'},order:[['due_at','ASC']],limit:5}),
  AgentHierarchy.findOne({where:{downline_id:req.user.id,depth:1}})
 ]);
 const upline=relation?await User.findByPk(relation.upline_id):null;
 res.json({agentCode:req.user.agent_code,summary:{totalProjects:projects.length,dealCount:projects.filter(x=>x.status==='deal').length,followUpCount:projects.filter(x=>x.status==='follow_up').length,myCommission:round(commissions.reduce((s,c)=>s+num(c.total_commission_amount),0))},upline:upline?{agentCode:upline.agent_code,name:upline.name}:null,upcomingTasks:tasks.map(x=>({id:x.id,title:x.title,dueAt:x.due_at,status:x.status})),recentProjects:projects.slice(0,10).map(p=>({projectCode:p.project_code,title:p.client_name,status:p.status,stage:p.execution_stage,dealValue:num(p.deal_value)}))});
}));
module.exports=r;
