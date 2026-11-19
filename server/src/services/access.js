const { Op,fail,id }=require("../utils/common");
const { AgentHierarchy,Project }=require("../models");
async function children(uplineId) {
 const rows=await AgentHierarchy.findAll({where:{upline_id:uplineId,depth:1},attributes:["downline_id"]});
 return rows.map(x=>Number(x.downline_id));
}
async function readAgentIds(user){if(user.role==="admin")return null;if(user.role==="upline")return [Number(user.id),...(await children(user.id))];return [Number(user.id)];}
async function projectScope(user){const ids=await readAgentIds(user);return ids===null?{}:{agent_id:{[Op.in]:ids}};}
async function findProject(user,projectId,editable=false,transaction){
 const ownerWhere=editable?(user.role==="admin"?{}:{agent_id:user.id}):await projectScope(user);
 const project=await Project.findOne({where:{id:id(projectId),...ownerWhere},transaction});
 if(!project)throw fail(404,"Proyek tidak ditemukan atau tidak diizinkan");return project;
}
function publicProject(p,user,agentCode){const own=user.role==="admin"||Number(p.agent_id)===Number(user.id);return {id:Number(p.id),projectCode:p.project_code,agentId:Number(p.agent_id),agentCode:agentCode||null,clientId:own?p.client_id:null,clientName:own?p.client_name:`Proyek ${agentCode||"Downline"}`,clientPhone:own?p.client_phone:null,clientEmail:own?p.client_email:null,status:p.status,stage:p.execution_stage,dealValue:Number(p.deal_value||0),cancelReason:own?p.cancel_reason:null,dealDate:p.deal_date,createdAt:p.created_at,updatedAt:p.updated_at,isAnonymized:!own};}
async function canReadClient(user,clientId){if(user.role==="admin")return true;const {Client}=require("../models");const c=await Client.findByPk(clientId);return c&&Number(c.created_by)===Number(user.id);}
module.exports={children,readAgentIds,projectScope,findProject,publicProject,canReadClient};
