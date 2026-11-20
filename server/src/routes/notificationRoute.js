const r=require('express').Router();const {Notification,User}=require('../models');
const {auth,roles}=require('../middleware/auth');const {notify,push}=require('../services/notify');
const {wrap,fail,id,nonempty,optional,page,Op}=require('../utils/common');
r.use(auth);
r.get('/',wrap(async(req,res)=>res.json(await Notification.findAll({where:{user_id:req.user.id},order:[['created_at','DESC']],...page(req)}))));
r.patch('/:id/read',wrap(async(req,res)=>{
 const n=await Notification.findOne({where:{id:id(req.params.id),user_id:req.user.id}});if(!n)throw fail(404,'Notifikasi tidak ditemukan');await n.update({is_read:true});res.json({ok:true});
}));
r.patch('/read-all/now',wrap(async(req,res)=>{const [updated]=await Notification.update({is_read:true},{where:{user_id:req.user.id,is_read:false}});res.json({updated});}));
r.post('/device',wrap(async(req,res)=>{
 const token=optional(req.body.fcmToken,4096);
 if(token){
  // Satu perangkat/token aktif untuk satu akun pada satu waktu.
  await User.update({fcm_token:null},{where:{fcm_token:token,id:{[Op.ne]:req.user.id}}});
 }
 await req.user.update({fcm_token:token});
 res.json({ok:true});
}));
r.post('/broadcast',roles('admin'),wrap(async(req,res)=>{
 const title=nonempty(req.body.title),message=nonempty(req.body.message,4000);
 const allowedRoles=['admin','upline','downline'];
 const inputRoles=req.body.roles;
 if(inputRoles!==undefined&&(!Array.isArray(inputRoles)||!inputRoles.length||inputRoles.some(x=>!allowedRoles.includes(x))))
   throw fail(400,'roles harus array admin/upline/downline');
 const codes=req.body.agentCodes;
 if(codes!==undefined&&(!Array.isArray(codes)||!codes.length||codes.length>500||codes.some(x=>typeof x!=='string'||!x.trim()||x.length>20)))
   throw fail(400,'agentCodes harus array kode agen (maksimal 500)');
 const where={is_active:true};
 if(inputRoles)where.role={[Op.in]:inputRoles};
 if(codes)where.agent_code={[Op.in]:[...new Set(codes.map(x=>x.trim().toUpperCase()))]};
 const targets=await User.findAll({where,attributes:['id']});
 if(!targets.length)throw fail(404,'Tidak ada penerima aktif yang cocok');
 for(const u of targets)await notify(u.id,title,message,'broadcast');
 let pushed=0;
 for(let i=0;i<targets.length;i+=25){
  const results=await Promise.allSettled(targets.slice(i,i+25).map(u=>push(u.id,title,message)));
  pushed+=results.filter(x=>x.status==='fulfilled'&&x.value).length;
 }
 res.status(201).json({ok:true,recipients:targets.length,pushSent:pushed});
}));
module.exports=r;
