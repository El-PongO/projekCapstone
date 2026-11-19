const test=require('node:test'),assert=require('node:assert/strict');
const {amount,pct,nonempty,date,monthWib,round}=require('../src/utils/common');
test('validasi nilai uang',()=>{assert.equal(amount('2000.50'),2000.5);assert.throws(()=>amount(-1));assert.throws(()=>amount('abc'));assert.throws(()=>amount(1.567));});
test('validasi rate dan teks',()=>{assert.equal(pct(10.123),10.12);assert.throws(()=>pct(150));assert.equal(nonempty(' Budi '),'Budi');assert.throws(()=>nonempty(''));});
test('format tanggal dan WIB',()=>{assert.equal(date('2026-11-12'),'2026-11-12');assert.throws(()=>date('12/11/2026'));assert.match(monthWib(),/^\d{4}-\d{2}$/);});
test('round dua digit',()=>{assert.equal(round(1.234),1.23);assert.equal(round(2.345),2.35);});
