'use strict';
const test = require('node:test');
const assert = require('node:assert/strict');
const zlib = require('node:zlib');
const PDFDocument = require('pdfkit');
const {drawProposal} = require('../src/services/proposalPresentation');

async function render(lines) {
  const pdf = new PDFDocument({size:'A4',margin:45});
  const chunks = [];
  pdf.on('data', chunk => chunks.push(chunk));
  const completed = new Promise((resolve,reject) => {
    pdf.on('end',resolve);
    pdf.on('error',reject);
  });
  const total = drawProposal(pdf, {
    title:'Proposal Sistem', number:'PRP-1', projectCode:'PRJ-1',
    clientName:'Klien A', validUntil:'2026-12-01', createdAt:new Date('2026-11-26T12:00:00Z'), lines
  });
  pdf.end();
  await completed;
  const buffer = Buffer.concat(chunks);
  const raw = buffer.toString('latin1');
  const streams = [];
  for (const match of raw.matchAll(/stream\r?\n/g)) {
    const end = raw.indexOf('endstream',match.index + match[0].length);
    if (end < 0) continue;
    try { streams.push(zlib.inflateSync(buffer.subarray(match.index + match[0].length,end)).toString('latin1')); }
    catch (error) { if (error.code !== 'Z_DATA_ERROR') throw error; }
  }
  const text = streams.map(stream => [...stream.matchAll(/<([0-9a-f]+)>/gi)]
    .map(match => Buffer.from(match[1],'hex').toString('latin1')).join('')).join('');
  return {raw, streams, text, total};
}

test('proposal has labeled table and preserves amounts and project details', async () => {
  const result = await render([
    {item_name:'Sistem Klinik',description:'Implementasi',quantity:2,unit_price:'1250.50'},
    {item_name:'Layanan',description:null,quantity:1,unit_price:'100'}
  ]);
  assert.equal(result.total,2601);
  for (const value of ['Proposal Sistem','PRP-1','PRJ-1','Klien A','Deskripsi','Jumlah',
    'Harga Satuan','Subtotal','Sistem Klinik','Implementasi','2.501','2.601','TOTAL PENAWARAN']) {
    assert.ok(result.text.includes(value), `Missing ${value}`);
  }
  assert.ok(result.streams.some(stream => /0\.098039\d* 0\.274509\d* 0\.721568\d* scn/.test(stream)));
  assert.ok(result.streams.some(stream => (stream.match(/\bre\b/g) || []).length >= 13 && /\nS\n/.test(stream)));
});

test('long proposal rows span pages and repeat the table headings', async () => {
  const lines = Array.from({length:50},(_,i) => ({
    item_name:`Item ${i}`,description:i===0 ? 'X'.repeat(3000)+'AKHIRDATA' : 'Deskripsi',
    quantity:1,unit_price:'10'
  }));
  const result = await render(lines);
  assert.equal(result.total,500);
  assert.ok((result.raw.match(/\/Type\s*\/Page\b/g) || []).length > 2);
  assert.ok((result.text.match(/Subtotal/g) || []).length > 2);
  assert.ok(result.text.includes('AKHIRDATA'));
  assert.ok(result.text.includes('Item 49'));
  assert.ok(result.text.includes('TOTAL PENAWARAN'));
});
