const r=require('express').Router();const {Client,Project}=require('../models');
const {auth}=require('../middleware/auth');const {wrap,fail,id,nonempty,optional,page,Op}=require('../utils/common');
r.use(auth);
r.get('/',wrap(async(req,res)=>{
 const where=req.user.role==='admin'?{}:{created_by:req.user.id};
 const rows=await Client.findAll({where,order:[['id','DESC']],...page(req)});
 res.json(rows);
}));
r.post('/',wrap(async(req,res)=>{
 const c=await Client.create({client_code:nonempty(req.body.clientCode,50),client_name:nonempty(req.body.clientName),phone_number:optional(req.body.phoneNumber,20),email:optional(req.body.email,100),address:optional(req.body.address,3000),created_by:req.user.id});
 res.status(201).json(c);
}));
r.get('/:id',wrap(async(req,res)=>{const x=await Client.findByPk(id(req.params.id));if(!x||req.user.role!=='admin'&&Number(x.created_by)!==Number(req.user.id))throw fail(404,'Klien tidak ditemukan');res.json(x);}));
r.patch('/:id',wrap(async(req,res)=>{
 const x=await Client.findByPk(id(req.params.id));if(!x||req.user.role!=='admin'&&Number(x.created_by)!==Number(req.user.id))throw fail(404,'Klien tidak ditemukan');
 const v={};for(const [key,col,max] of [['clientName','client_name',255],['phoneNumber','phone_number',20],['email','email',100],['address','address',3000]])if(req.body[key]!==undefined)v[col]=key==='clientName'?nonempty(req.body[key],max):optional(req.body[key],max);
 await x.update(v);res.json(x);
}));
r.delete('/:id',wrap(async(req,res)=>{
 const x=await Client.findByPk(id(req.params.id));if(!x||req.user.role!=='admin'&&Number(x.created_by)!==Number(req.user.id))throw fail(404,'Klien tidak ditemukan');
 if(await Project.count({where:{client_id:x.id}}))throw fail(409,'Klien sudah digunakan proyek');await x.destroy();res.json({ok:true});
}));
module.exports=r;
