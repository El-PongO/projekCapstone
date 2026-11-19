const {User,AgentHierarchy,Commission}=require("../models");
const {num,round}=require("../utils/common");
async function assignDealCommissions(project,transaction){
 const agent=await User.findByPk(project.agent_id,{transaction});
 const recipients=[agent];
 const hierarchy=await AgentHierarchy.findOne({where:{downline_id:project.agent_id,depth:1},transaction});
 if(hierarchy){const upline=await User.findByPk(hierarchy.upline_id,{transaction});if(upline?.is_active&&upline.role==="upline")recipients.push(upline);}
 const rows=[];
 for(const user of recipients){
   const kind=user.agent_type||"internal";
   const rate=kind==="external"?num(user.rate_value_percent):0;
   const value=round(num(project.deal_value)*rate/100);
   // Angka upline/agent adalah rate masing-masing yang disimpan Admin, bukan pembagian otomatis dari komisi orang lain.
   const [c]=await Commission.findOrCreate({where:{project_id:project.id,agent_id:user.id},defaults:{agent_type:kind,commission_rate_percent:rate,total_commission_amount:value,payout_status:"pending"},transaction});
   rows.push(c);
 }
 return rows;
}
module.exports={assignDealCommissions};
