const {User,AgentHierarchy,Commission,DealRateOffer}=require('../models');
const {num,round,fail}=require('../utils/common');

async function commissionRecipients(project,transaction){
 const agent=await User.findByPk(project.agent_id,{transaction});
 if(!agent||!agent.is_active)throw fail(409,'Agen proyek tidak aktif');
 const recipients=[agent];
 const hierarchy=await AgentHierarchy.findOne({where:{downline_id:project.agent_id,depth:1},transaction});
 if(hierarchy){
  const upline=await User.findByPk(hierarchy.upline_id,{transaction});
  if(upline?.is_active&&upline.role==='upline')recipients.push(upline);
 }
 return recipients;
}
// Setiap penerima eksternal harus menerima rate per proyek sebelum Deal disahkan.
async function acceptedRates(project,transaction){
 const recipients=await commissionRecipients(project,transaction);
 const externals=recipients.filter(u=>u.agent_type==='external');
 const offers=await DealRateOffer.findAll({where:{project_id:project.id},transaction});
 const accepted=new Map();
 for(const user of externals){
  const offer=offers.find(o=>Number(o.agent_id)===Number(user.id));
  if(!offer||offer.status!=='accepted')throw fail(409,`Rate komisi ${user.agent_code} belum disepakati`);
  accepted.set(Number(user.id),num(offer.rate_percent));
 }
 const total=round([...accepted.values()].reduce((s,n)=>s+n,0));
 if(total>100)throw fail(409,'Total rate komisi penerima melebihi 100%');
 return {recipients,accepted};
}
async function assignDealCommissions(project,transaction){
 const {recipients,accepted}=await acceptedRates(project,transaction);
 const rows=[];
 for(const user of recipients){
  if(user.agent_type!=='external')continue; // Internal adalah pendapatan PT, bukan komisi agen.
  const rate=accepted.get(Number(user.id));
  const value=round(num(project.deal_value)*rate/100);
  if(value<=0)continue;
  const [commission]=await Commission.findOrCreate({where:{project_id:project.id,agent_id:user.id},defaults:{agent_type:'external',commission_rate_percent:rate,total_commission_amount:value,payout_status:'pending'},transaction});
  rows.push(commission);
 }
 return rows;
}
module.exports={commissionRecipients,acceptedRates,assignDealCommissions};
