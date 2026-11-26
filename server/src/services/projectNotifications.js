const {User,AgentHierarchy}=require('../models');
const {notify,push}=require('./notify');

async function recordProjectUpdate(project,actorId,title,message,transaction){
 const [admins,owner,relation]=await Promise.all([
  User.findAll({where:{role:'admin',is_active:true},attributes:['id'],transaction}),
  User.findByPk(project.agent_id,{transaction}),
  AgentHierarchy.findOne({where:{downline_id:project.agent_id,depth:1},transaction})
 ]);
 const recipients=new Set(admins.map(user=>Number(user.id)));
 if(owner?.is_active)recipients.add(Number(owner.id));
 if(relation){
  const upline=await User.findByPk(relation.upline_id,{transaction});
  if(upline?.is_active&&upline.role==='upline')recipients.add(Number(upline.id));
 }
 recipients.delete(Number(actorId));
 const recipientIds=[...recipients];
 for(const recipientId of recipientIds){
  await notify(recipientId,title,message,'project_status',transaction);
 }
 return recipientIds;
}

async function pushProjectUpdate(recipientIds,title,message){
 await Promise.allSettled(recipientIds.map(recipientId=>push(recipientId,title,message)));
}

module.exports={recordProjectUpdate,pushProjectUpdate};
