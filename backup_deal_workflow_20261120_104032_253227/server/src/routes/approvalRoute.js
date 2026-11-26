const r=require('express').Router(),db=require('../config/database');
const {Project,ProjectApproval,User,AgentHierarchy}=require('../models');
const {auth,roles}=require('../middleware/auth');
const {wrap,fail,id,nonempty,enumValue,page}=require('../utils/common');
const {findProject}=require('../services/access');const {log}=require('../services/activity');
const {assignDealCommissions}=require('../services/commission');const {notify,push}=require('../services/notify');
r.use(auth);
r.post('/projects/:projectId/request',roles('upline','downline'),wrap(async(req,res)=>{
 const p=await findProject(req.user,req.params.projectId,true);
 if(p.status!=='follow_up'||Number(p.deal_value)<=0)throw fail(409,'Hanya proyek Follow Up dengan nilai Deal >0 yang bisa diajukan');
 const a=await db.transaction(async transaction=>{
  await Project.findByPk(p.id,{transaction,lock:transaction.LOCK.UPDATE});
  if(await ProjectApproval.count({where:{project_id:p.id,status:'pending'},transaction}))throw fail(409,'Pengajuan masih pending');
  return ProjectApproval.create({project_id:p.id,requested_by:req.user.id,status:'pending'},{transaction});
 });res.status(201).json(a);
}));
r.get('/',roles('admin'),wrap(async(req,res)=>{
 const where={};if(req.query.status)where.status=enumValue(req.query.status,['pending','approved','rejected']);
 res.json(await ProjectApproval.findAll({where,order:[['created_at','DESC']],...page(req)}));
}));
r.get('/mine',roles('upline','downline'),wrap(async(req,res)=>res.json(await ProjectApproval.findAll({where:{requested_by:req.user.id},order:[['created_at','DESC']],...page(req)}))));
r.post('/:id/review',roles('admin'),wrap(async(req,res)=>{
 const decision=enumValue(req.body.decision,['approved','rejected']);
 const reason=decision==='rejected'?nonempty(req.body.reason,2000):null;
 const result=await db.transaction(async transaction=>{
  const a=await ProjectApproval.findByPk(id(req.params.id),{transaction,lock:transaction.LOCK.UPDATE});
  if(!a)throw fail(404,'Approval tidak ditemukan');if(a.status!=='pending')throw fail(409,'Approval sudah diproses');
  const p=await Project.findByPk(a.project_id,{transaction,lock:transaction.LOCK.UPDATE});
  if(!p||p.status!=='follow_up')throw fail(409,'Project sudah berubah status');
  let commissions=[];
  if(decision==='approved'){
   await p.update({status:'deal',deal_date:require('../utils/common').dateWib(),cancel_reason:null},{transaction});
   commissions=await assignDealCommissions(p,transaction);
  }
  await a.update({status:decision,reviewed_by:req.user.id,reason,reviewed_at:new Date()},{transaction});
  await log(p.id,req.user.id,`Approval Deal ${decision}${reason?' : '+reason:''}`,p.execution_stage,p.execution_stage,transaction);
  await notify(a.requested_by,'Hasil Approval Proyek',`Proyek ${p.project_code}: ${decision}`,'project_status',transaction);
  return {approval:a,project:p,commissions};
 });
 // FCM best effort; not a prerequisite for DB commit.
 await push(result.approval.requested_by,'Hasil Approval Proyek',`Proyek ${result.project.project_code}: ${decision}`);
 res.json({approval:result.approval,projectStatus:result.project.status,commissions:result.commissions});
}));
module.exports=r;
