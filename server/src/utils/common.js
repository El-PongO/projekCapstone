const crypto = require("crypto");
const { Op } = require("sequelize");
function fail(status, message) { const error = new Error(message); error.status = status; return error; }
function id(x) { const n = Number(x); if (!Number.isSafeInteger(n) || n <= 0) throw fail(400, "ID tidak valid"); return n; }
function nonempty(x, max = 255) { if (typeof x !== "string" || !x.trim() || x.trim().length > max) throw fail(400, "Teks wajib diisi (maksimum "+max+" karakter)"); return x.trim(); }
function optional(x, max=255) { return x === null || x === undefined || x === "" ? null : nonempty(x,max); }
function amount(x, positive=false) { const n=Number(x); if(!Number.isFinite(n) || n<0 || n>100000000000 || (positive&&n===0) || Math.round(n*100)!==n*100 && Math.abs(Math.round(n*100)-n*100)>1e-6) throw fail(400,"Nominal tidak valid (maksimal 2 angka desimal)");return Math.round(n*100)/100; }
function pct(x) { const n=Number(x); if(!Number.isFinite(n)||n<0||n>100) throw fail(400,"Persentase harus 0-100"); return Math.round(n*100)/100; }
function enumValue(x, options) { if(!options.includes(x)) throw fail(400,`Nilai harus salah satu: ${options.join(', ')}`); return x; }
function date(x) { if(typeof x!=="string"|| !/^\d{4}-\d{2}-\d{2}$/.test(x)||Number.isNaN(Date.parse(x+"T00:00:00Z")))throw fail(400,"Format tanggal harus YYYY-MM-DD");if(new Date(x+'T00:00:00Z').toISOString().slice(0,10)!==x)throw fail(400,'Tanggal kalender tidak valid');return x; }
function datetime(x) { const d = new Date(x); if(!x||Number.isNaN(d.valueOf()))throw fail(400,"Tanggal dan waktu tidak valid");return d; }
function code(prefix) { return `${prefix}-${Date.now().toString(36).toUpperCase()}-${crypto.randomBytes(3).toString('hex').toUpperCase()}`; }
function num(x) { return Number(x || 0); }
function page(req) {const limit=Math.max(1,Math.min(100,Number(req.query.limit)||30));const offset=Math.max(0,Math.min(100000,Number(req.query.offset)||0));return { limit,offset }; }
function dateWib(d=new Date()) { return new Intl.DateTimeFormat("sv-SE",{ timeZone:"Asia/Jakarta",year:"numeric",month:"2-digit",day:"2-digit" }).format(d); }
function monthWib(d=new Date()) {return dateWib(d).slice(0,7);}
function round(x) { return Math.round((Number(x)+Number.EPSILON)*100)/100; }
function safeUser(row) { const u=row.toJSON?row.toJSON():row;return {id:Number(u.id),agentCode:u.agent_code,name:u.name,email:u.email,role:u.role,agentType:u.agent_type,ratePercent:num(u.rate_value_percent),phoneNumber:u.phone_number,isActive:!!u.is_active}; }
const wrap = handler => (req,res,next)=>Promise.resolve(handler(req,res,next)).catch(next);
module.exports={ Op,fail,id,nonempty,optional,amount,pct,enumValue,date,datetime,code,num,page,dateWib,monthWib,round,safeUser,wrap };
