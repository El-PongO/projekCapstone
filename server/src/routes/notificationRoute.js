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
 const token=optional(req.body.fcmToken,4096);await req.user.update({fcm_token:token});res.json({ok:true});
}));
r.post('/broadcast',roles('admin'),wrap(async(req,res)=>{
 const title=nonempty(req.body.title),message=nonempty(req.body.message,4000);
 const targets=await User.findAll({where:{is_active:true},attributes:['id']});
 for(const u of targets)await notify(u.id,title,message,'broadcast');
 // Push FCM best-effort dengan batch terbatas, kegagalan 1 device tidak membatalkan record database.
 let pushed=0;
 for(let i=0;i<targets.length;i+=25){const results=await Promise.allSettled(targets.slice(i,i+25).map(u=>push(u.id,title,message)));pushed+=results.filter(x=>x.status==='fulfilled'&&x.value).length;}
 res.status(201).json({ok:true,recipients:targets.length,pushSent:pushed});
}));
module.exports=r;
