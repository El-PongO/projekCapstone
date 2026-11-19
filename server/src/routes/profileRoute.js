const r=require('express').Router(),bcrypt=require('bcryptjs');
const {User}=require('../models');const {auth}=require('../middleware/auth');
const {wrap,fail,optional,nonempty,safeUser}=require('../utils/common');
r.use(auth);
r.get('/',(req,res)=>res.json(safeUser(req.user)));
r.patch('/',wrap(async(req,res)=>{
 const values={};
 if(req.body.name!==undefined)values.name=nonempty(req.body.name);
 if(req.body.phoneNumber!==undefined)values.phone_number=optional(req.body.phoneNumber,20);
 if(req.body.fcmToken!==undefined)values.fcm_token=optional(req.body.fcmToken,4096);
 // Email dan rate hanya boleh diubah Admin untuk menghindari spoofing/keuangan.
 if(!Object.keys(values).length)throw fail(400,'Tidak ada data yang diubah');
 await req.user.update(values);res.json(safeUser(req.user));
}));
r.post('/change-password',wrap(async(req,res)=>{
 const {oldPassword,newPassword}=req.body;
 if(typeof oldPassword!=='string'||!(await bcrypt.compare(oldPassword,req.user.password)))throw fail(401,'Password lama salah');
 if(typeof newPassword!=='string'||newPassword.length<8||newPassword.length>72)throw fail(400,'Password baru 8 sampai 72 karakter');
 await req.user.update({password:await bcrypt.hash(newPassword,12)});res.json({ok:true});
}));
module.exports=r;
