'use strict';
const test=require('node:test');
const assert=require('node:assert/strict');
const express=require('express');
const jwt=require('jsonwebtoken');

process.env.JWT_SECRET='master-route-test-secret-that-is-long-enough';
let categoryItemCount=1;
let projectItemCount=1;
let deletedCategory=false;
let deletedItem=false;
const category={
 id:5,category_name:'Software',description:'Original',
 update:async values=>Object.assign(category,values),
 destroy:async()=>{deletedCategory=true;}
};
const item={
 id:9,category_id:5,item_name:'CRM',base_price:100,description:'Original',
 update:async values=>Object.assign(item,values),
 destroy:async()=>{deletedItem=true;}
};
const modelsPath=require.resolve('../src/models');
require.cache[modelsPath]={
 id:modelsPath,filename:modelsPath,loaded:true,
 exports:{
  User:{findByPk:async id=>Number(id)===1?{id:1,role:'admin',is_active:true}:null},
  Category:{findByPk:async id=>Number(id)===5?category:null},
  Item:{
   findByPk:async id=>Number(id)===9?item:null,
   count:async()=>categoryItemCount
  },
  ProjectItem:{count:async()=>projectItemCount}
 }
};

const app=express();
app.use(express.json());
app.use('/api/master',require('../src/routes/masterRoute'));
app.use((error,req,res,next)=>res.status(error.status||500).json({error:error.message}));
let server;
let base;
const token=jwt.sign({sub:'1'},process.env.JWT_SECRET);
const request=(path,options={})=>fetch(base+path,{
 ...options,
 headers:{Authorization:`Bearer ${token}`,'Content-Type':'application/json',...options.headers}
});

test.before(async()=>{
 server=await new Promise(resolve=>{
  const listener=app.listen(0,'127.0.0.1',()=>resolve(listener));
 });
 base=`http://127.0.0.1:${server.address().port}/api/master`;
});
test.after(async()=>new Promise(resolve=>server.close(resolve)));

test('master item updates persist and dependent records cannot be deleted',async()=>{
 let response=await request('/categories/5',{
  method:'PATCH',body:JSON.stringify({categoryName:'Business Software',description:'Updated'})
 });
 assert.equal(response.status,200);
 assert.equal((await response.json()).category_name,'Business Software');

 response=await request('/items/9',{
  method:'PATCH',body:JSON.stringify({categoryId:5,itemName:'CRM Plus',basePrice:250})
 });
 assert.equal(response.status,200);
 assert.equal((await response.json()).item_name,'CRM Plus');
 assert.equal(item.base_price,250);

 response=await request('/categories/5',{method:'DELETE'});
 assert.equal(response.status,409);
 assert.equal(deletedCategory,false);

 categoryItemCount=0;
 response=await request('/items/9',{method:'DELETE'});
 assert.equal(response.status,409);
 assert.equal(deletedItem,false);

 projectItemCount=0;
 response=await request('/items/9',{method:'DELETE'});
 assert.equal(response.status,200);
 assert.equal(deletedItem,true);

 response=await request('/categories/5',{method:'DELETE'});
 assert.equal(response.status,200);
 assert.equal(deletedCategory,true);
});
