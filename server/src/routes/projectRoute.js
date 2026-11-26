const r=require('express').Router();const db=require('../config/database');
const {Project,ProjectItem,Item,User,Client,ProjectActivityLog,ProjectApproval,AgentHierarchy}=require('../models');
const {notify,push}=require('../services/notify');
const {auth}=require('../middleware/auth');const {findProject,projectScope,publicProject}=require('../services/access');
const {log}=require('../services/activity');
const {wrap,fail,id,nonempty,optional,amount,enumValue,page,Op}=require('../utils/common');
r.use(auth);
const statuses=['follow_up','deal','no_deal','cancelled'];const stages=['negotiation','proposal_sent','app_creation','implementation','completed'];
async function agentValid(agentId){const agent=await User.findByPk(agentId);if(!agent||!agent.is_active||!['upline','downline'].includes(agent.role))throw fail(400,'Agen penanggung jawab tidak valid');return agent;}
async function normalizeItems(items){
 if(!Array.isArray(items)||items.length>50)throw fail(400,'items harus array maksimal 50');
 const found=new Set(),rows=[];
 for(const entry of items){const iid=id(entry.itemId);if(found.has(iid))throw fail(400,'Item tidak boleh duplikat');found.add(iid);
  const item=await Item.findByPk(iid);if(!item)throw fail(400,'Item tidak ditemukan');
  const quantity=id(entry.quantity);if(quantity>100000)throw fail(400,'Jumlah item terlalu besar');
  rows.push({item_id:iid,quantity,price:amount(entry.price??item.base_price)});
 }return rows;
}
async function projectCode(){const {code}=require('../utils/common');return code('PRJ');}
r.get('/',wrap(async(req,res)=>{
 const where=await projectScope(req.user);
 if(req.query.status)where.status=enumValue(req.query.status,statuses);
 if(req.query.agentId){const a=id(req.query.agentId);if(req.user.role==='admin')where.agent_id=a;else{const allowed=await require('../services/access').readAgentIds(req.user);if(!allowed.includes(a))throw fail(403,'Agen tidak diizinkan');where.agent_id=a;}}
 const projects=await Project.findAll({where,order:[['created_at','DESC']],...page(req)});
 const ids=[...new Set(projects.map(p=>Number(p.agent_id)))];const agents=await User.findAll({where:{id:{[Op.in]:ids}},attributes:['id','agent_code']});const codes=new Map(agents.map(x=>[Number(x.id),x.agent_code]));
 res.json(projects.map(p=>publicProject(p,req.user,codes.get(Number(p.agent_id)))));
}));
r.post('/',wrap(async(req,res)=>{
 const agentId=req.user.role==='admin'?id(req.body.agentId):Number(req.user.id);await agentValid(agentId);
 const items=await normalizeItems(req.body.items??[]);
 const clientId=req.body.clientId==null?null:id(req.body.clientId);
 let client=null;
 if(clientId){client=await Client.findByPk(clientId);if(!client)throw fail(400,'Klien tidak ditemukan');if(req.user.role!=='admin'&&Number(client.created_by)!==Number(req.user.id))throw fail(403,'Tidak dapat memakai klien agen lain');}
 const clientName=client?client.client_name:nonempty(req.body.clientName);
 const clientPhone=client?client.phone_number:optional(req.body.clientPhone,20);
 const clientEmail=client?client.email:optional(req.body.clientEmail,100);
 const dealValue=amount(req.body.dealValue??0);
 const project=await db.transaction(async transaction=>{
  const row=await Project.create({project_code:await projectCode(),agent_id:agentId,client_id:clientId,client_name:clientName,client_phone:clientPhone,client_email:clientEmail,status:'follow_up',execution_stage:'negotiation',deal_value:dealValue},{transaction});
  for(const i of items)await ProjectItem.create({...i,project_id:row.id},{transaction});
  await log(row.id,req.user.id,'Proyek dibuat',null,'negotiation',transaction);
  return row;
 });res.status(201).json(publicProject(project,req.user));
}));
r.get('/:id',wrap(async(req,res)=>{
 const p=await findProject(req.user,req.params.id);
 const agent=await User.findByPk(p.agent_id);const out=publicProject(p,req.user,agent?.agent_code);
 if(out.isAnonymized)return res.json({project:out,items:[],activities:[]});
 const [items,activities]=await Promise.all([ProjectItem.findAll({where:{project_id:p.id}}),ProjectActivityLog.findAll({where:{project_id:p.id},order:[['created_at','DESC']],limit:100})]);
 res.json({project:out,items,activities});
}));
r.patch('/:id',wrap(async(req,res)=>{
 const p=await findProject(req.user,req.params.id,true);
 if(p.status==='deal')throw fail(409,'Proyek Deal tidak dapat diubah secara langsung');
 if(await ProjectApproval.count({where:{project_id:p.id,status:'pending'}}))throw fail(409,'Proyek sedang menunggu approval');
 const updates={};
 for(const [key,col,max] of [['clientName','client_name',255],['clientPhone','client_phone',20],['clientEmail','client_email',100]])if(req.body[key]!==undefined)updates[col]=key==='clientName'?nonempty(req.body[key],max):optional(req.body[key],max);
 if(req.body.dealValue!==undefined)updates.deal_value=amount(req.body.dealValue);
 if(!Object.keys(updates).length)throw fail(400,'Tidak ada perubahan');
 await p.update(updates);await log(p.id,req.user.id,'Informasi proyek diperbarui',p.execution_stage,p.execution_stage);
 res.json(publicProject(p,req.user));
}));
r.put('/:id/items',wrap(async(req,res)=>{
 const p=await findProject(req.user,req.params.id,true);
 if(p.status==='deal'||await ProjectApproval.count({where:{project_id:p.id,status:'pending'}}))throw fail(409,'Items terkunci untuk Deal atau pending approval');
 const items=await normalizeItems(req.body.items);
 await db.transaction(async transaction=>{
  await ProjectItem.destroy({where:{project_id:p.id},transaction});
  for(const item of items)await ProjectItem.create({...item,project_id:p.id},{transaction});
  await log(p.id,req.user.id,'Rincian item diubah',p.execution_stage,p.execution_stage,transaction);
 });res.json({ok:true,items});
}));
r.patch('/:id/status',wrap(async(req,res)=>{
 const p=await findProject(req.user,req.params.id,true);const to=enumValue(req.body.status,['follow_up','no_deal','cancelled']);
 if(p.status==='deal')throw fail(409,'Proyek Deal tidak dapat diubah tanpa prosedur Admin');
 if(await ProjectApproval.count({where:{project_id:p.id,status:'pending'}}))throw fail(409,'Pending approval harus diputuskan dahulu');
 const reason=to==='follow_up'?null:nonempty(req.body.reason,2000);const from=p.status;
 await db.transaction(async transaction=>{await p.update({status:to,cancel_reason:reason},{transaction});await log(p.id,req.user.id,`Status ${from} -> ${to}${reason?' : '+reason:''}`,p.execution_stage,p.execution_stage,transaction);});
 res.json(publicProject(p,req.user));
}));
r.patch('/:id/stage',wrap(async(req,res)=>{
 const p=await findProject(req.user,req.params.id,true);
 const to=enumValue(req.body.stage,stages);
 if(['app_creation','implementation','completed'].includes(to)&&p.status!=='deal')throw fail(409,'Tahap pelaksanaan hanya untuk proyek Deal');
 if(to==='completed'&&(req.user.role==='admin'||Number(p.agent_id)!==Number(req.user.id)))
  throw fail(403,'Hanya agen pemilik proyek yang boleh menyatakan proyek selesai');
 const admins=to==='completed'?await User.findAll({where:{role:'admin',is_active:true},attributes:['id']}):[];
 const result=await db.transaction(async transaction=>{
  const locked=await Project.findByPk(p.id,{transaction,lock:transaction.LOCK.UPDATE});
  if(locked.execution_stage==='completed')throw fail(409,'Proyek telah selesai; stage tidak boleh diubah lagi');
  if(locked.status!=='deal'&&['app_creation','implementation','completed'].includes(to))throw fail(409,'Proyek belum Deal');
  const from=locked.execution_stage;
  if(from===to)throw fail(409,'Stage proyek tidak berubah');
  await locked.update({execution_stage:to},{transaction});
  await log(locked.id,req.user.id,`Stage ${from} -> ${to}`,from,to,transaction);
  const recipients=[];
  if(to==='completed'){
   const relation=await AgentHierarchy.findOne({where:{downline_id:locked.agent_id,depth:1},transaction});
   for(const u of admins)recipients.push(Number(u.id));
   if(relation)recipients.push(Number(relation.upline_id));
   for(const uid of [...new Set(recipients)])await notify(uid,'Proyek Selesai',`${locked.project_code} ditandai selesai oleh ${req.user.agent_code}. Tinjau pembayaran dan komisi`,'project_status',transaction);
  }
  return {project:locked,recipients:[...new Set(recipients)]};
 });
 if(to==='completed')await Promise.allSettled(result.recipients.map(uid=>push(uid,'Proyek Selesai',`${p.project_code}: tinjau pencairan komisi`)));
 res.json(publicProject(result.project,req.user));
}));
r.delete('/:id',wrap(async(req,res)=>{
 const p=await findProject(req.user,req.params.id,true);if(p.status==='deal')throw fail(409,'Proyek Deal tidak dapat dibatalkan');
 if(await ProjectApproval.count({where:{project_id:p.id,status:'pending'}}))throw fail(409,'Pending approval harus diputuskan dahulu');
 const reason=nonempty(req.body?.reason||'Dibatalkan pengguna',2000);
 await db.transaction(async transaction=>{await p.update({status:'cancelled',cancel_reason:reason},{transaction});await log(p.id,req.user.id,`Proyek dibatalkan: ${reason}`,p.execution_stage,p.execution_stage,transaction);});
 res.json({ok:true,message:'Proyek ditandai cancelled, riwayat tidak dihapus'});
}));

// Counter Offer: tidak menambah enum status baru, hanya mengubah nilai negosiasi + mencatat log.
r.post('/:id/counter-offer',wrap(async(req,res)=>{
 const p=await findProject(req.user,req.params.id,true);
 if(p.status!=='follow_up')throw fail(409,'Counter Offer hanya untuk proyek Follow Up');
 if(await ProjectApproval.count({where:{project_id:p.id,status:'pending'}}))throw fail(409,'Approval masih pending');
 const newValue=amount(req.body.newDealValue,true),notes=nonempty(req.body.notes||'Negosiasi ulang',1000);
 await db.transaction(async transaction=>{
  const oldValue=p.deal_value;
  await p.update({deal_value:newValue,execution_stage:'negotiation'},{transaction});
  await log(p.id,req.user.id,`Counter Offer dari ${oldValue} menjadi ${newValue}: ${notes}`,p.execution_stage,'negotiation',transaction);
 });res.json(publicProject(p,req.user));
}));

module.exports=r;
