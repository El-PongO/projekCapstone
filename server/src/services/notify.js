const {Notification,User}=require("../models");
let firebase=null;
function initFirebase(){
 const key=process.env.FIREBASE_SERVICE_ACCOUNT;
 if(!key)return;
 try {const admin=require("firebase-admin");const service=require(require("path").resolve(key));
  if(!admin.apps.length)admin.initializeApp({credential:admin.credential.cert(service)});firebase=admin;
  console.log("Firebase Admin aktif");
 }catch(e){console.error("Firebase tidak aktif:",e.message);}
}
async function notify(userId,title,message,type="system",transaction){
 const notification=await Notification.create({user_id:userId,title,message,type,is_read:false},{transaction});
 // Push FCM dilakukan setelah transaksi commit oleh caller bila perlu, agar tidak mengirim notifikasi pada rollback.
 return notification;
}
async function push(userId,title,message){
 if(!firebase)return false;
 const user=await User.findByPk(userId);if(!user?.fcm_token)return false;
 try{await firebase.messaging().send({token:user.fcm_token,notification:{title,body:message}});return true;}
 catch(e){console.error("FCM gagal:",e.message);return false;}
}
module.exports={initFirebase,notify,push};
