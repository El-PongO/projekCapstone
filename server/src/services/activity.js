const {ProjectActivityLog}=require("../models");
async function log(projectId,agentId,message,previous=null,current=null,transaction){
 return ProjectActivityLog.create({project_id:projectId,agent_id:agentId,activity_description:message,previous_stage:previous,current_stage:current},{transaction});
}
module.exports={log};
