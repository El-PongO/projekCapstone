require('dotenv').config();
const bcrypt=require('bcryptjs');const db=require('../config/database');
const {User,AgentHierarchy,Category,Item,Client,Project,FollowUpTask}=require('../models');
async function main(){
 if(process.env.NODE_ENV==='production')throw new Error('Seeder dilarang di production');
 await db.authenticate();
 const pw=process.env.SEED_DEMO_PASSWORD||'password123';if(pw.length<8)throw new Error('SEED_DEMO_PASSWORD minimal 8 karakter');
 const hash=await bcrypt.hash(pw,12);
 for(const [code,name,email,role] of [
  ['ADM-001','Admin Integra','admin@integra.local','admin'],
  ['UPL-001','Budi Upline','upline@integra.local','upline'],
  ['DNL-001','Dina Downline','downline@integra.local','downline']
 ]){
  const existing=await User.findOne({where:{agent_code:code}});
  if(!existing)await User.create({agent_code:code,name,email,password:hash,role,agent_type:role==='admin'?null:'external',rate_value_percent:role==='admin'?0:role==='upline'?5:10,is_active:true});
 }
 const up=await User.findOne({where:{agent_code:'UPL-001'}}),down=await User.findOne({where:{agent_code:'DNL-001'}});
 if(up&&down)await AgentHierarchy.findOrCreate({where:{downline_id:down.id},defaults:{upline_id:up.id,depth:1}});
 const [category]=await Category.findOrCreate({where:{category_name:'Software Development'},defaults:{description:'Aplikasi dan website'}});
 await Item.findOrCreate({where:{item_code:'ITM-001'},defaults:{category_id:category.id,item_name:'Pembuatan Website',base_price:15000000}});
 const [client]=await Client.findOrCreate({where:{client_code:'CL-001'},defaults:{client_name:'PT Demo Sejahtera',created_by:up.id,email:'demo@example.com'}});
 const [project]=await Project.findOrCreate({where:{project_code:'PRJ-DEMO-001'},defaults:{agent_id:up.id,client_id:client.id,client_name:client.client_name,client_email:client.email,status:'follow_up',execution_stage:'negotiation',deal_value:15000000}});
 await FollowUpTask.findOrCreate({where:{project_id:project.id,title:'Telepon kembali klien'},defaults:{agent_id:up.id,due_at:new Date(Date.now()+24*60*60*1000),status:'pending'}});
 console.log('Seed selesai. Akun baru dibuat jika belum ada; password akun yang sudah ada TIDAK diubah.');
 console.log('Login demo: ADM-001 / UPL-001 / DNL-001; password baca SEED_DEMO_PASSWORD pada .env');
}
main().catch(e=>{console.error('Seed gagal:',e);process.exitCode=1;}).finally(()=>db.close());
