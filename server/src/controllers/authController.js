const bcrypt=require('bcryptjs');
const jwt=require('jsonwebtoken');
const {User}=require('../models');
const {fail,nonempty}=require('../utils/common');
module.exports={ login: async(req,res)=>{
 const identifier=nonempty(req.body?.identifier,255);
 const password=req.body?.password;
 if(typeof password!=='string'||!password)throw fail(400,'Password wajib diisi');
 const {Op}=require('sequelize');
 const user=await User.findOne({where:{[Op.or]:[{agent_code:identifier},{email:identifier}]}});
 if(!user||!user.is_active||!(await bcrypt.compare(password,user.password)))throw fail(401,'Email/kode agen atau password salah');
 const token=jwt.sign({sub:String(user.id)},process.env.JWT_SECRET,{algorithm:'HS256',expiresIn:process.env.JWT_EXPIRES_IN||'8h'});
 res.json({token,user:{id:Number(user.id),agentCode:user.agent_code,name:user.name,email:user.email,role:user.role}});
} };
