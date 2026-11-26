const r=require('express').Router(),fs=require('fs'),path=require('path');
const PDFDocument=require('pdfkit');const {finished}=require('stream/promises');
const db=require('../config/database');
const {Proposal,ProposalItem,Item}=require('../models');
const {auth}=require('../middleware/auth');const {findProject}=require('../services/access');
const {wrap,fail,id,nonempty,optional,amount,date,code,page}=require('../utils/common');
const {drawProposal}=require('../services/proposalPresentation');
const folder=path.resolve(__dirname,'../../uploads/proposals');fs.mkdirSync(folder,{recursive:true});
r.use(auth);
async function proposalRows(values){
 if(!Array.isArray(values)||values.length===0||values.length>50)throw fail(400,'Proposal wajib memiliki 1-50 item');
 const rows=[];
 for(const v of values){
  const item=v.itemId?await Item.findByPk(id(v.itemId)):null;
  if(v.itemId&&!item)throw fail(400,'Item master tidak ditemukan');
  rows.push({item_id:item?.id||null,item_name:nonempty(v.itemName||item?.item_name),description:optional(v.description,1000),quantity:id(v.quantity),unit_price:amount(v.unitPrice??item?.base_price)});
 }return rows;
}
r.get('/',wrap(async(req,res)=>{
 const where=req.user.role==='admin'?{}:{agent_id:req.user.id};
 res.json(await Proposal.findAll({where,order:[['created_at','DESC']],...page(req)}));
}));
r.post('/projects/:projectId',wrap(async(req,res)=>{
 const p=await findProject(req.user,req.params.projectId,true);
 const lines=await proposalRows(req.body.items);
 const title=nonempty(req.body.title||`Proposal ${p.project_code}`),validUntil=req.body.validUntil?date(req.body.validUntil):null;
 const number=code('PRP'),basename=number+'.pdf',filepath=path.join(folder,basename);
 try{
  const pdf=new PDFDocument({size:'A4',margin:45});const file=fs.createWriteStream(filepath);pdf.pipe(file);
  const total=drawProposal(pdf,{title,number,projectCode:p.project_code,clientName:p.client_name,validUntil,createdAt:new Date(),lines});
  pdf.end();await finished(file);
  const result=await db.transaction(async transaction=>{
   const rec=await Proposal.create({proposal_number:number,project_id:p.id,agent_id:req.user.id,proposal_title:title,pdf_file_path:'proposals/'+basename,valid_until:validUntil},{transaction});
   for(const x of lines)await ProposalItem.create({...x,proposal_id:rec.id},{transaction});
   return rec;
  });res.status(201).json({proposal:result,total,items:lines});
 }catch(e){fs.rmSync(filepath,{force:true});throw e;}
}));
r.get('/:id',wrap(async(req,res)=>{
 const p=await Proposal.findByPk(id(req.params.id));if(!p||req.user.role!=='admin'&&Number(p.agent_id)!==Number(req.user.id))throw fail(404,'Proposal tidak ditemukan');
 const lines=await ProposalItem.findAll({where:{proposal_id:p.id}});res.json({proposal:p,items:lines});
}));
r.get('/:id/download',wrap(async(req,res)=>{
 const p=await Proposal.findByPk(id(req.params.id));if(!p||req.user.role!=='admin'&&Number(p.agent_id)!==Number(req.user.id))throw fail(404,'Proposal tidak ditemukan');
 const safe=path.basename(p.pdf_file_path),full=path.join(folder,safe);
 if(!fs.existsSync(full))throw fail(404,'File PDF tidak ditemukan');res.download(full,safe);
}));
module.exports=r;
