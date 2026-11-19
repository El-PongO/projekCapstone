const r=require('express').Router();
const {auth}=require('../middleware/auth');
const {wrap}=require('../utils/common');
const controller=require('../controllers/dashboardController');
r.use(auth);
r.get('/',wrap(controller.get));
module.exports=r;
