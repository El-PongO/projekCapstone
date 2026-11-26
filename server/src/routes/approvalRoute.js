const r=require('express').Router(),db=require('../config/database');
const {Project,ProjectApproval,User,AgentHierarchy,DealRateOffer}=require('../models');
const {auth,roles}=require('../middleware/auth');
const {wrap,fail,id,nonempty,enumValue,page,pct}=require('../utils/common');
const {findProject}=require('../services/access');
const {log}=require('../services/activity');
const {commissionRecipients,assignDealCommissions}=require('../services/commission');
const {notify,push}=require('../services/notify');
r.use(auth);

r.post('/projects/:projectId/request',roles('upline','downline'),wrap(async(req,res)=>{
 const project=await findProject(req.user,req.params.projectId,true);
 if(project.status!=='follow_up'||Number(project.deal_value)<=0)throw fail(409,'Hanya proyek Follow Up bernilai positif yang bisa diajukan');
 const admins=await User.findAll({where:{role:'admin',is_active:true},attributes:['id']});
 const result=await db.transaction(async transaction=>{
  const locked=await Project.findByPk(project.id,{transaction,lock:transaction.LOCK.UPDATE});
  if(locked.status!=='follow_up')throw fail(409,'Status proyek sudah berubah');
  if(await ProjectApproval.count({where:{project_id:project.id,status:'pending'},transaction}))throw fail(409,'Pengajuan Deal masih pending');
  // Pengajuan baru harus disertai kesepakatan rate baru, bukan memakai hasil permintaan lama.
  await DealRateOffer.destroy({where:{project_id:project.id},transaction});
  const a=await ProjectApproval.create({project_id:project.id,requested_by:req.user.id,status:'pending'},{transaction});
  for(const admin of admins)await notify(admin.id,'Pengajuan Deal',`Agen mengajukan Deal ${project.project_code}; sepakati rate terlebih dahulu`,'project_status',transaction);
  return a;
 });
 await Promise.allSettled(admins.map(a=>push(a.id,'Pengajuan Deal',`Proyek ${project.project_code} menunggu rate dan persetujuan`)));
 res.status(201).json(result);
}));

r.get('/',roles('admin'),wrap(async(req,res)=>{
 const where={};if(req.query.status)where.status=enumValue(req.query.status,['pending','approved','rejected']);
 res.json(await ProjectApproval.findAll({where,order:[['created_at','DESC']],...page(req)}));
}));
r.get('/mine',roles('upline','downline'),wrap(async(req,res)=>res.json(await ProjectApproval.findAll({where:{requested_by:req.user.id},order:[['created_at','DESC']],...page(req)}))));

// Admin memeriksa penerima: owner, dan Upline langsung jika owner Downline.
r.get('/:id/rates',roles('admin'),wrap(async(req,res)=>{
 const a=await ProjectApproval.findByPk(id(req.params.id));
 if(!a)throw fail(404,'Pengajuan tidak ditemukan');
 const p=await Project.findByPk(a.project_id);
 const recipients=await commissionRecipients(p);
 const offers=await DealRateOffer.findAll({where:{project_id:p.id}});
 res.json({approvalId:Number(a.id),projectId:Number(p.id),approvalStatus:a.status,
  recipients:recipients.map(u=>({agentId:Number(u.id),agentCode:u.agent_code,role:u.role,
   agentType:u.agent_type,defaultRate:Number(u.rate_value_percent||0),
   offer:(()=>{const o=offers.find(v=>Number(v.agent_id)===Number(u.id));return o?{id:Number(o.id),ratePercent:Number(o.rate_percent),status:o.status}:null;})()}))});
}));

// Rate ditetapkan untuk proyek ini, bukan mengubah rate global di Master Agent.
r.put('/:id/rates/:agentId',roles('admin'),wrap(async(req,res)=>{
 const rate=pct(req.body.ratePercent),agentId=id(req.params.agentId),approvalId=id(req.params.id);
 const result=await db.transaction(async transaction=>{
  const a=await ProjectApproval.findByPk(approvalId,{transaction,lock:transaction.LOCK.UPDATE});
  if(!a||a.status!=='pending')throw fail(409,'Pengajuan Deal tidak lagi pending');
  const p=await Project.findByPk(a.project_id,{transaction,lock:transaction.LOCK.UPDATE});
  if(!p||p.status!=='follow_up')throw fail(409,'Proyek bukan Follow Up');
  const recipients=await commissionRecipients(p,transaction);
  const recipient=recipients.find(u=>Number(u.id)===agentId&&u.agent_type==='external');
  if(!recipient)throw fail(403,'Agen bukan penerima komisi eksternal proyek ini');
  let offer=await DealRateOffer.findOne({where:{project_id:p.id,agent_id:agentId},transaction,lock:transaction.LOCK.UPDATE});
  if(offer){
   if(offer.status==='accepted'&&Number(offer.rate_percent)===rate)return {offer,agentId,projectCode:p.project_code,unchanged:true};
   await offer.update({rate_percent:rate,status:'pending',proposed_by:req.user.id,proposed_at:new Date(),responded_at:null},{transaction});
  }else offer=await DealRateOffer.create({project_id:p.id,agent_id:agentId,rate_percent:rate,status:'pending',proposed_by:req.user.id,proposed_at:new Date()},{transaction});
  await notify(agentId,'Penawaran Rate Komisi',`Proyek ${p.project_code}: Admin menawarkan komisi ${rate}%. Silakan setujui atau tolak`,'commission',transaction);
  return {offer,agentId,projectCode:p.project_code};
 });
 if(!result.unchanged)await push(agentId,'Penawaran Rate Komisi',`Proyek ${result.projectCode}: ${rate}%. Buka detail proyek untuk merespons.`);
 res.json({id:Number(result.offer.id),ratePercent:Number(result.offer.rate_percent),status:result.offer.status});
}));

r.get('/my-rate-offers',roles('upline','downline'),wrap(async(req,res)=>{
 const where={agent_id:req.user.id};
 if(req.query.projectId)where.project_id=id(req.query.projectId);
 const offers=await DealRateOffer.findAll({where,order:[['proposed_at','DESC']],...page(req)});
 const ids=[...new Set(offers.map(o=>Number(o.project_id)))];
 const projects=await Project.findAll({where:{id:ids},attributes:['id','project_code','status']});
 const codes=new Map(projects.map(p=>[Number(p.id),p]));
 res.json(offers.map(o=>({id:Number(o.id),projectId:Number(o.project_id),projectCode:codes.get(Number(o.project_id))?.project_code||'',ratePercent:Number(o.rate_percent),status:o.status,projectStatus:codes.get(Number(o.project_id))?.status||''})));
}));

r.post('/rates/:id/respond',roles('upline','downline'),wrap(async(req,res)=>{
 const decision=enumValue(req.body.decision,['accepted','rejected']);
 const result=await db.transaction(async transaction=>{
  const offer=await DealRateOffer.findByPk(id(req.params.id),{transaction,lock:transaction.LOCK.UPDATE});
  if(!offer||Number(offer.agent_id)!==Number(req.user.id))throw fail(404,'Penawaran rate tidak ditemukan');
  const p=await Project.findByPk(offer.project_id,{transaction,lock:transaction.LOCK.UPDATE});
  if(!p||p.status!=='follow_up')throw fail(409,'Proyek sudah disahkan atau tidak aktif');
  if(!await ProjectApproval.count({where:{project_id:p.id,status:'pending'},transaction}))throw fail(409,'Tidak ada pengajuan Deal yang pending');
  if(offer.status!=='pending')throw fail(409,'Penawaran sudah direspons; minta Admin mengirim penawaran ulang');
  await offer.update({status:decision,responded_at:new Date()},{transaction});
  const admins=await User.findAll({where:{role:'admin',is_active:true},attributes:['id'],transaction});
  for(const u of admins)await notify(u.id,'Respons Rate Komisi',`${req.user.agent_code} ${decision} rate ${Number(offer.rate_percent)}% untuk ${p.project_code}`,'commission',transaction);
  return {offer,admins,projectCode:p.project_code};
 });
 await Promise.allSettled(result.admins.map(a=>push(a.id,'Respons Rate Komisi',`${req.user.agent_code}: ${decision} • ${result.projectCode}`)));
 res.json({id:Number(result.offer.id),status:result.offer.status,ratePercent:Number(result.offer.rate_percent)});
}));

r.post('/:id/review',roles('admin'),wrap(async(req,res)=>{
 const decision=enumValue(req.body.decision,['approved','rejected']);
 const reason=decision==='rejected'?nonempty(req.body.reason,2000):null;
 const result=await db.transaction(async transaction=>{
  const a=await ProjectApproval.findByPk(id(req.params.id),{transaction,lock:transaction.LOCK.UPDATE});
  if(!a)throw fail(404,'Approval tidak ditemukan');
  if(a.status!=='pending')throw fail(409,'Pengajuan sudah diproses');
  const p=await Project.findByPk(a.project_id,{transaction,lock:transaction.LOCK.UPDATE});
  if(!p||p.status!=='follow_up')throw fail(409,'Proyek sudah berubah status');
  let commissions=[];
  if(decision==='approved'){
   // Menggagalkan approval jika ada rate eksternal yang belum diterima.
   commissions=await assignDealCommissions(p,transaction);
   await p.update({status:'deal',deal_date:require('../utils/common').dateWib(),cancel_reason:null},{transaction});
  }
  await a.update({status:decision,reviewed_by:req.user.id,reason,reviewed_at:new Date()},{transaction});
  await log(p.id,req.user.id,`Pengajuan Deal ${decision}${reason?' : '+reason:''}`,p.execution_stage,p.execution_stage,transaction);
  const relation=await AgentHierarchy.findOne({where:{downline_id:p.agent_id,depth:1},transaction});
  const recipients=[...new Set([Number(a.requested_by),Number(p.agent_id),...(relation?[Number(relation.upline_id)]:[])])];
  for(const uid of recipients)await notify(uid,'Keputusan Deal',`Proyek ${p.project_code}: ${decision}`,'project_status',transaction);
  return {approval:a,project:p,commissions,recipients};
 });
 await Promise.allSettled(result.recipients.map(uid=>push(uid,'Keputusan Deal',`Proyek ${result.project.project_code}: ${decision}`)));
 res.json({approval:result.approval,projectStatus:result.project.status,commissions:result.commissions});
}));
module.exports=r;
