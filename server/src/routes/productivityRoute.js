const r=require('express').Router(),fs=require('fs'),path=require('path'),crypto=require('crypto'),multer=require('multer');
const {FollowUpTask,ProjectMilestone,Meeting,ProjectDocument,Project,User}=require('../models');
const {auth,roles}=require('../middleware/auth');const {findProject,projectScope}=require('../services/access');
const {wrap,fail,id,nonempty,optional,datetime,date,enumValue,page,Op}=require('../utils/common');
r.use(auth);
// Follow-up tasks. Admin reads all; agents can manage only their own tasks.
r.get('/tasks',wrap(async(req,res)=>{
 const where=req.user.role==='admin'?{}:{agent_id:req.user.id};
 if(req.query.status)where.status=enumValue(req.query.status,['pending','completed','cancelled']);
 res.json(await FollowUpTask.findAll({where,order:[['due_at','ASC']],...page(req)}));
}));
r.post('/projects/:id/tasks',roles('admin','upline','downline'),wrap(async(req,res)=>{
 const project=await findProject(req.user,req.params.id,true);
 const a=req.user.role==='admin'&&req.body.agentId? id(req.body.agentId):Number(project.agent_id);
 if(Number(a)!==Number(project.agent_id))throw fail(400,'Task harus ditugaskan kepada pemilik proyek');
 const task=await FollowUpTask.create({project_id:project.id,agent_id:a,title:nonempty(req.body.title),due_at:datetime(req.body.dueAt),status:'pending',notes:optional(req.body.notes,3000)});
 res.status(201).json(task);
}));
r.patch('/tasks/:id',wrap(async(req,res)=>{
 const t=await FollowUpTask.findByPk(id(req.params.id));if(!t||req.user.role!=='admin'&&Number(t.agent_id)!==Number(req.user.id))throw fail(404,'Task tidak ditemukan');
 const v={};if(req.body.title!==undefined)v.title=nonempty(req.body.title);if(req.body.dueAt!==undefined)v.due_at=datetime(req.body.dueAt);if(req.body.status!==undefined)v.status=enumValue(req.body.status,['pending','completed','cancelled']);if(req.body.notes!==undefined)v.notes=optional(req.body.notes,3000);
 await t.update(v);res.json(t);
}));
// My Schedule: follow-up tasks + meetings, sorted by date.
r.get('/schedule',wrap(async(req,res)=>{
 const where=req.user.role==='admin'&&req.query.agentId?{agent_id:id(req.query.agentId)}:{agent_id:req.user.id};
 const [tasks,meetings]=await Promise.all([
  FollowUpTask.findAll({where,order:[['due_at','ASC']],limit:100}),
  Meeting.findAll({where,order:[['meeting_at','ASC']],limit:100})
 ]);
 res.json([...tasks.map(t=>({kind:'task',id:Number(t.id),title:t.title,start:t.due_at,status:t.status,projectId:Number(t.project_id)})),...meetings.map(m=>({kind:'meeting',id:Number(m.id),title:m.meeting_title,start:m.meeting_at,status:m.status,projectId:Number(m.project_id)}))].sort((a,b)=>new Date(a.start)-new Date(b.start)));
}));
// Meetings.
r.get('/meetings',wrap(async(req,res)=>res.json(await Meeting.findAll({where:req.user.role==='admin'&&req.query.agentId?{agent_id:id(req.query.agentId)}:{agent_id:req.user.id},order:[['meeting_at','ASC']],...page(req)}))));
r.post('/projects/:id/meetings',wrap(async(req,res)=>{
 const p=await findProject(req.user,req.params.id,true);const at=datetime(req.body.meetingAt),end=req.body.meetingEndAt?datetime(req.body.meetingEndAt):null;
 if(end&&end<=at)throw fail(400,'Waktu selesai harus setelah waktu mulai');
 const m=await Meeting.create({project_id:p.id,agent_id:p.agent_id,meeting_title:nonempty(req.body.meetingTitle),meeting_at:at,meeting_end_at:end,location:optional(req.body.location),agenda:optional(req.body.agenda,5000),status:'scheduled',notes:optional(req.body.notes,3000)});
 res.status(201).json(m);
}));
r.patch('/meetings/:id',wrap(async(req,res)=>{
 const m=await Meeting.findByPk(id(req.params.id));if(!m||req.user.role!=='admin'&&Number(m.agent_id)!==Number(req.user.id))throw fail(404,'Meeting tidak ditemukan');
 const v={};if(req.body.meetingTitle!==undefined)v.meeting_title=nonempty(req.body.meetingTitle);if(req.body.meetingAt!==undefined)v.meeting_at=datetime(req.body.meetingAt);if(req.body.meetingEndAt!==undefined)v.meeting_end_at=req.body.meetingEndAt===null?null:datetime(req.body.meetingEndAt);if(req.body.location!==undefined)v.location=optional(req.body.location);if(req.body.agenda!==undefined)v.agenda=optional(req.body.agenda,5000);if(req.body.status!==undefined)v.status=enumValue(req.body.status,['scheduled','completed','cancelled']);if(req.body.notes!==undefined)v.notes=optional(req.body.notes,3000);
 if(v.meeting_end_at||m.meeting_end_at){const end=v.meeting_end_at===undefined?m.meeting_end_at:v.meeting_end_at;if(end&&new Date(end)<=new Date(v.meeting_at||m.meeting_at))throw fail(400,'Waktu selesai tidak valid');}
 await m.update(v);res.json(m);
}));
// Milestones (status workflow).
r.get('/projects/:id/milestones',wrap(async(req,res)=>{const p=await findProject(req.user,req.params.id);if(req.user.role!=='admin'&&Number(req.user.id)!==Number(p.agent_id))return res.json([]);res.json(await ProjectMilestone.findAll({where:{project_id:p.id},order:[['due_date','ASC'],['id','ASC']]}));}));
r.post('/projects/:id/milestones',wrap(async(req,res)=>{
 const p=await findProject(req.user,req.params.id,true);const m=await ProjectMilestone.create({project_id:p.id,title:nonempty(req.body.title),description:optional(req.body.description,4000),status:'pending',due_date:req.body.dueDate?date(req.body.dueDate):null});res.status(201).json(m);
}));
r.patch('/milestones/:id',wrap(async(req,res)=>{
 const m=await ProjectMilestone.findByPk(id(req.params.id));if(!m)throw fail(404,'Milestone tidak ditemukan');await findProject(req.user,m.project_id,true);
 const v={};if(req.body.title!==undefined)v.title=nonempty(req.body.title);if(req.body.description!==undefined)v.description=optional(req.body.description,4000);if(req.body.dueDate!==undefined)v.due_date=req.body.dueDate===null?null:date(req.body.dueDate);if(req.body.status!==undefined){v.status=enumValue(req.body.status,['pending','in_progress','completed']);v.completed_at=v.status==='completed'?new Date():null;}await m.update(v);res.json(m);
}));
r.delete('/milestones/:id',wrap(async(req,res)=>{const m=await ProjectMilestone.findByPk(id(req.params.id));if(!m)throw fail(404,'Milestone tidak ditemukan');await findProject(req.user,m.project_id,true);await m.destroy();res.json({ok:true});}));
// Private document upload / download; PDF and common images only, max 5MB.
const uploadDir=path.resolve(__dirname,'../../uploads/documents');fs.mkdirSync(uploadDir,{recursive:true});
const mimeExt={'application/pdf':'.pdf','image/png':'.png','image/jpeg':'.jpg'};
const upload=multer({storage:multer.diskStorage({destination:uploadDir,filename:(req,file,cb)=>cb(null,crypto.randomBytes(18).toString('hex')+mimeExt[file.mimetype])}),limits:{fileSize:5*1024*1024},fileFilter:(req,file,cb)=>mimeExt[file.mimetype]?cb(null,true):cb(fail(400,'File harus PDF/PNG/JPG'))});
r.get('/projects/:id/documents',wrap(async(req,res)=>{const p=await findProject(req.user,req.params.id,true);res.json(await ProjectDocument.findAll({where:{project_id:p.id},attributes:['id','project_id','uploaded_by','document_type','original_filename','created_at']}));}));
r.post('/projects/:id/documents',wrap(async(req,res,next)=>{await findProject(req.user,req.params.id,true);next();}),upload.single('file'),wrap(async(req,res)=>{
 if(!req.file)throw fail(400,'File wajib dikirim sebagai multipart file');
 const header=Buffer.alloc(8);const fd=fs.openSync(req.file.path,'r');let got=0;
 try{got=fs.readSync(fd,header,0,8,0);}finally{fs.closeSync(fd);}
 const kind=req.file.mimetype;
 const valid=(kind==='application/pdf'&&header.subarray(0,5).toString()==='%PDF-') ||
   (kind==='image/png'&&header.subarray(0,8).equals(Buffer.from([137,80,78,71,13,10,26,10]))) ||
   (kind==='image/jpeg'&&got>=3&&header[0]===255&&header[1]===216&&header[2]===255);
 if(!valid){fs.rmSync(req.file.path,{force:true});throw fail(400,'Konten file tidak sesuai tipe PDF/PNG/JPG');}
 try{const d=await ProjectDocument.create({project_id:id(req.params.id),uploaded_by:req.user.id,document_type:nonempty(req.body.documentType||'other',50),original_filename:path.basename(req.file.originalname).slice(0,255),file_path:'documents/'+req.file.filename});res.status(201).json({id:d.id,documentType:d.document_type,filename:d.original_filename});}
 catch(e){fs.rmSync(req.file.path,{force:true});throw e;}
}));
r.get('/documents/:id/download',wrap(async(req,res)=>{
 const d=await ProjectDocument.findByPk(id(req.params.id));if(!d)throw fail(404,'Dokumen tidak ditemukan');await findProject(req.user,d.project_id,true);
 const filename=path.basename(d.file_path);const loc=path.join(uploadDir,filename);if(!fs.existsSync(loc))throw fail(404,'File fisik tidak ditemukan');res.download(loc,d.original_filename);
}));
r.delete('/documents/:id',wrap(async(req,res)=>{const d=await ProjectDocument.findByPk(id(req.params.id));if(!d)throw fail(404,'Dokumen tidak ditemukan');await findProject(req.user,d.project_id,true);await d.destroy();fs.rmSync(path.join(uploadDir,path.basename(d.file_path)),{force:true});res.json({ok:true});}));
// Satu endpoint ringkas untuk tampilan progress, jadwal dan lampiran pada aplikasi.
r.get('/projects/:id/overview',wrap(async(req,res)=>{
 const p=await findProject(req.user,req.params.id);
 const owner=req.user.role==='admin'||Number(p.agent_id)===Number(req.user.id);
 const [milestones,tasks,meetings,documents]=await Promise.all([
  ProjectMilestone.findAll({where:{project_id:p.id},order:[['due_date','ASC'],['id','ASC']]}),
  FollowUpTask.findAll({where:{project_id:p.id},order:[['due_at','ASC']]}),
  Meeting.findAll({where:{project_id:p.id},order:[['meeting_at','ASC']]}),
  ProjectDocument.findAll({where:{project_id:p.id},attributes:['id','document_type','original_filename','created_at']})
 ]);
 const completed=milestones.filter(x=>x.status==='completed').length;
 res.json({projectId:Number(p.id),progressPercent:milestones.length?Math.round(100*completed/milestones.length):0,
  completedMilestones:completed,totalMilestones:milestones.length,canEdit:owner,
  milestones:owner?milestones:[],tasks:owner?tasks:[],meetings:owner?meetings:[],documents:owner?documents:[]});
}));
module.exports=r;
