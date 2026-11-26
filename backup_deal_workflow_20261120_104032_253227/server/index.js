require('dotenv').config();
const express=require('express'),cors=require('cors');
const db=require('./src/config/database');const {initFirebase}=require('./src/services/notify');
const app=express();app.disable('x-powered-by');
if(!process.env.JWT_SECRET||process.env.JWT_SECRET.length<32)throw new Error('JWT_SECRET wajib minimal 32 karakter di file .env');
const origins=(process.env.CORS_ORIGINS||'').split(',').map(x=>x.trim()).filter(Boolean);
app.use(cors(origins.length?{origin:origins}:{origin:true}));
app.use(express.json({limit:'1mb'}));
app.use(express.urlencoded({extended:false,limit:'1mb'}));
if(process.env.NODE_ENV!=='production')app.use((req,res,next)=>{res.on('finish',()=>console.log(`[API] ${req.method} ${req.path} ${res.statusCode}`));next();});
app.get('/api/health',(req,res)=>res.json({ok:true,service:'Integra Agency Backend',tables:20}));
app.get('/api/health/db',async(req,res)=>{try{await db.authenticate();res.json({ok:true,database:'connected'});}catch{res.status(503).json({ok:false,database:'unavailable'});}});
app.use('/api/auth',require('./src/routes/authRoute'));
app.use('/api/profile',require('./src/routes/profileRoute'));
app.use('/api/agents',require('./src/routes/agentRoute'));
app.use('/api/master',require('./src/routes/masterRoute'));
app.use('/api/clients',require('./src/routes/clientRoute'));
app.use('/api/projects',require('./src/routes/projectRoute'));
app.use('/api/approvals',require('./src/routes/approvalRoute'));
app.use('/api/dashboard',require('./src/routes/dashboardRoute'));
app.use('/api/performance',require('./src/routes/performanceRoute'));
app.use('/api/upline',require('./src/routes/uplineRoute'));
app.use('/api/downline',require('./src/routes/downlineRoute'));
app.use('/api/admin',require('./src/routes/adminRoute'));
app.use('/api/productivity',require('./src/routes/productivityRoute'));
const finance=require('./src/routes/financeRoute');
app.use('/api/invoices',finance.invoices);
app.use('/api/payments',finance.payments);
app.use('/api/commissions',finance.commissions);
app.use('/api/commission-payouts',finance.payouts);
app.use('/api/proposals',require('./src/routes/proposalRoute'));
app.use('/api/notifications',require('./src/routes/notificationRoute'));
app.use('/api/leaderboard',require('./src/routes/leaderboardRoute'));
app.use('/api/reports',require('./src/routes/reportRoute'));
app.use((req,res)=>res.status(404).json({error:'Endpoint tidak ditemukan'}));
app.use((err,req,res,next)=>{
 if(res.headersSent)return next(err);
 let status=err.status||500,message=err.message||'Internal server error';
 if(err.name==='SequelizeUniqueConstraintError'){status=409;message='Data unik sudah digunakan';}
 if(err.name==='SequelizeForeignKeyConstraintError'){status=409;message='Data sedang digunakan oleh tabel lain atau foreign key tidak valid';}
 if(err.name==='SequelizeValidationError'){status=400;message=err.errors?.map(x=>x.message).join(', ')||'Validasi gagal';}
 if(err.name==='MulterError'){status=400;message=err.code==='LIMIT_FILE_SIZE'?'File maksimal 5MB':err.message;}
 if(status>=500){console.error('[SERVER ERROR]',err);message='Terjadi kesalahan pada server';}
 res.status(status).json({error:message});
});
async function start(){
 await db.authenticate();initFirebase();
 const port=Number(process.env.PORT||3001);
 app.listen(port,'0.0.0.0',()=>console.log(`Backend Integra Agency berjalan http://127.0.0.1:${port}`));
 require('./src/jobs/scheduler').startReminderScheduler();
}
if(require.main===module)start().catch(e=>{console.error('Gagal memulai Backend:',e.message);process.exit(1);});
module.exports=app;
