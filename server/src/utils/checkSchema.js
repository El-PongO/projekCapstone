require("dotenv").config();
const db=require("../config/database");
const models=require("../models");
(async()=>{
 try{
  await db.authenticate();
  let failed=0;
  for(const model of Object.values(models)){
   const table=model.getTableName();const expected=Object.keys(model.getAttributes());
   let actual;
   try{actual=await db.getQueryInterface().describeTable(table);}catch(e){console.error(`TABEL HILANG: ${table}`);failed++;continue;}
   const missing=expected.filter(k=>!Object.hasOwn(actual,k));
   if(missing.length){console.error(`KOLOM HILANG ${table}: ${missing.join(', ')}`);failed++;}
   else console.log(`OK ${table}`);
  }
  process.exitCode=failed?1:0;
 }catch(e){console.error("Schema check gagal:",e.message);process.exitCode=1;}
 finally{await db.close();}
})();
