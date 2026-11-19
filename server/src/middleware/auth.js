const jwt=require("jsonwebtoken");
const { User }=require("../models");
const { fail,wrap }=require("../utils/common");
const auth=wrap(async(req,res,next)=>{
  const m=/^Bearer (.+)$/i.exec(req.headers.authorization||"");
  if(!m)throw fail(401,"Token diperlukan");
  let payload;
  try{payload=jwt.verify(m[1],process.env.JWT_SECRET,{algorithms:["HS256"]});}
  catch{throw fail(401,"Token kedaluwarsa atau tidak valid");}
  const uid=Number(payload.sub||payload.id);
  if(!Number.isSafeInteger(uid)||uid<=0)throw fail(401,"Token tidak valid");
  const user=await User.findByPk(uid);
  if(!user||!user.is_active)throw fail(401,"Akun tidak aktif atau tidak ditemukan");
  req.user=user;
  next();
});
const roles=(...allowed)=>(req,res,next)=>allowed.includes(req.user.role)?next():next(fail(403,"Akses ditolak untuk role ini"));
module.exports={auth,roles};
