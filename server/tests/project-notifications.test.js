'use strict';
const test=require('node:test');
const assert=require('node:assert/strict');

const notifications=[];
const pushes=[];
const modelsPath=require.resolve('../src/models');
const notifyPath=require.resolve('../src/services/notify');
require.cache[modelsPath]={
 id:modelsPath,filename:modelsPath,loaded:true,
 exports:{
  User:{
   findAll:async()=>[{id:1},{id:2}],
   findByPk:async id=> id===3
    ?{id:3,role:'downline',is_active:true}
    :id===4?{id:4,role:'upline',is_active:true}:null
  },
  AgentHierarchy:{findOne:async()=>({upline_id:4})}
 }
};
require.cache[notifyPath]={
 id:notifyPath,filename:notifyPath,loaded:true,
 exports:{
  notify:async(...args)=>notifications.push(args),
  push:async(...args)=>{pushes.push(args);return true;}
 }
};
const {recordProjectUpdate,pushProjectUpdate}=require('../src/services/projectNotifications');

test('project events notify active admins and the owner/upline, excluding the actor',async()=>{
 notifications.length=0;
 const recipients=await recordProjectUpdate(
  {id:12,agent_id:3,project_code:'PRJ-12'},
  3,'Pembaruan Status Proyek','Proyek PRJ-12: status follow_up -> cancelled',
  {id:'transaction'}
 );

 assert.deepEqual(recipients,[1,2,4]);
 assert.deepEqual(notifications.map(([userId])=>userId),recipients);
 assert.ok(notifications.every(([,title,message,type,transaction])=>
  title==='Pembaruan Status Proyek'&&message.includes('PRJ-12')&&
  type==='project_status'&&transaction.id==='transaction'));
});

test('push delivery is attempted for each persisted notification recipient',async()=>{
 pushes.length=0;
 await pushProjectUpdate([1,4],'Proyek Baru','Proyek PRJ-12 dibuat');
 assert.deepEqual(pushes,[
  [1,'Proyek Baru','Proyek PRJ-12 dibuat'],
  [4,'Proyek Baru','Proyek PRJ-12 dibuat']
 ]);
});
