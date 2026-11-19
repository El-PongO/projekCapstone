require('dotenv').config();
const {Op}=require('sequelize');const db=require('../config/database');
const {FollowUpTask,Notification,Meeting}=require('../models');const {notify,push,initFirebase}=require('../services/notify');
const hours=24;
async function run(){
 await db.authenticate();initFirebase();const now=new Date(),next=new Date(now.getTime()+hours*3600000);
 const tasks=await FollowUpTask.findAll({where:{status:'pending',due_at:{[Op.between]:[now,next]}}});
 let sent=0;
 for(const t of tasks){
  const message=`Follow-up #${t.id}: ${t.title} (jadwal ${new Date(t.due_at).toISOString()})`;
  const existing=await Notification.findOne({where:{user_id:t.agent_id,message,created_at:{[Op.gte]:new Date(Date.now()-24*3600000)}}});
  if(!existing){await notify(t.agent_id,'Pengingat Follow Up',message,'system');await push(t.agent_id,'Pengingat Follow Up',message);sent++;}
 }
 const meetings=await Meeting.findAll({where:{status:'scheduled',meeting_at:{[Op.between]:[now,next]}}});
 for(const m of meetings){
  const message=`Meeting #${m.id}: ${m.meeting_title} (jadwal ${new Date(m.meeting_at).toISOString()})`;
  const existing=await Notification.findOne({where:{user_id:m.agent_id,message,created_at:{[Op.gte]:new Date(Date.now()-24*3600000)}}});
  if(!existing){await notify(m.agent_id,'Pengingat Meeting',message,'system');await push(m.agent_id,'Pengingat Meeting',message);sent++;}
 }
 console.log(`Pengingat baru: ${sent}`);
}
run().catch(e=>{console.error('Reminder gagal:',e);process.exitCode=1;}).finally(()=>db.close());
