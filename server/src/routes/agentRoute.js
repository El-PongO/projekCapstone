const r=require('express').Router(),bcrypt=require('bcryptjs');
const db=require('../config/database');
const {User,AgentHierarchy}=require('../models');
const {auth,roles}=require('../middleware/auth');
const {wrap,fail,id,nonempty,optional,pct,page,safeUser,Op,enumValue}=require('../utils/common');
const {children}=require('../services/access');
r.use(auth);
r.get('/',roles('admin'),wrap(async(req,res)=>{
 const where={};if(req.query.role)where.role=enumValue(req.query.role,['admin','upline','downline']);
 const rows=await User.findAll({where,attributes:{exclude:['password','fcm_token']},order:[['id','ASC']],...page(req)});
 res.json(rows.map(safeUser));
}));
r.post('/',roles('admin'),wrap(async(req,res)=>{
 const role=enumValue(req.body.role,['upline','downline']);
 const email=nonempty(req.body.email),agentCode=nonempty(req.body.agentCode,20).toUpperCase();
 const password=nonempty(req.body.password,72);
 if(password.length<8)throw fail(400,'Password minimal 8 karakter');
 const parentId=req.body.uplineId==null?null:id(req.body.uplineId);
 if(role==='downline'&&parentId!==null){const parent=await User.findByPk(parentId);if(!parent||parent.role!=='upline'||!parent.is_active)throw fail(400,'Upline tidak valid');}
 if(role==='upline'&&parentId!==null)throw fail(400,'Upline tidak boleh memiliki parent Upline dalam model ini');
 const u=await db.transaction(async transaction=>{
  const user=await User.create({agent_code:agentCode,name:nonempty(req.body.name),email,password:await bcrypt.hash(password,12),phone_number:optional(req.body.phoneNumber,20),role,agent_type:enumValue(req.body.agentType||'external',['internal','external']),rate_value_percent:pct(req.body.ratePercent??0),is_active:true},{transaction});
  if(parentId)await AgentHierarchy.create({upline_id:parentId,downline_id:user.id,depth:1},{transaction});return user;
 });res.status(201).json(safeUser(u));
}));
r.patch('/:id',roles('admin'),wrap(async(req,res)=>{
 const u=await User.findByPk(id(req.params.id));if(!u)throw fail(404,'Agen tidak ditemukan');
 const x={};
 if(req.body.name!==undefined)x.name=nonempty(req.body.name);
 if(req.body.email!==undefined)x.email=nonempty(req.body.email);
 if(req.body.phoneNumber!==undefined)x.phone_number=optional(req.body.phoneNumber,20);
 if(req.body.agentType!==undefined)x.agent_type=enumValue(req.body.agentType,['internal','external']);
 if(req.body.ratePercent!==undefined)x.rate_value_percent=pct(req.body.ratePercent);
 if(req.body.isActive!==undefined){if(typeof req.body.isActive!=='boolean')throw fail(400,'isActive harus boolean');if(Number(u.id)===Number(req.user.id)&&req.body.isActive===false)throw fail(400,'Tidak boleh menonaktifkan akun sendiri');x.is_active=req.body.isActive;}
 if(req.body.password!==undefined){const pass=nonempty(req.body.password,72);if(pass.length<8)throw fail(400,'Password minimal 8 karakter');x.password=await bcrypt.hash(pass,12);}
 await u.update(x);res.json(safeUser(u));
}));
r.put('/:id/upline',roles('admin'),wrap(async(req,res)=>{
 const downline=await User.findByPk(id(req.params.id));if(!downline||downline.role!=='downline')throw fail(404,'Downline tidak ditemukan');
 const parentId=req.body.uplineId==null?null:id(req.body.uplineId);
 if(parentId){const up=await User.findByPk(parentId);if(!up||up.role!=='upline'||!up.is_active)throw fail(400,'Upline tidak ditemukan');}
 await db.transaction(async transaction=>{
  await AgentHierarchy.destroy({where:{downline_id:downline.id},transaction});
  if(parentId)await AgentHierarchy.create({upline_id:parentId,downline_id:downline.id,depth:1},{transaction});
 });res.json({ok:true,downlineId:Number(downline.id),uplineId:parentId});
}));
r.get('/my-upline/info',roles('downline'),wrap(async(req,res)=>{
 const relation=await AgentHierarchy.findOne({where:{downline_id:req.user.id,depth:1}});
 if(!relation)return res.json({upline:null});
 const up=await User.findByPk(relation.upline_id);res.json({upline:up?{agentCode:up.agent_code,name:up.name}:null});
}));
module.exports=r;
