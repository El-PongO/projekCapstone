'use strict';
const test = require('node:test');
const assert = require('node:assert/strict');
const express = require('express');
const jwt = require('jsonwebtoken');
const ExcelJS = require('exceljs');
const zlib = require('node:zlib');
const db = require('../src/config/database');

process.env.JWT_SECRET = 'reports-test-secret-that-is-long-enough';
let role = 'admin';
let rows = [];
let lastQuery;
const modelsPath = require.resolve('../src/models');
require.cache[modelsPath] = {
  id:modelsPath, filename:modelsPath, loaded:true,
  exports:{User:{findByPk:async () => ({id:1, role, is_active:true})}}
};
db.query = async (sql, options) => {
  lastQuery = {sql, options};
  return rows;
};
const app = express();
app.use('/api/reports', require('../src/routes/reportRoute'));
app.use((err, req, res, next) => res.status(err.status || 500).json({error:err.message}));
let server, base;
const token = jwt.sign({sub:'1'}, process.env.JWT_SECRET);
const request = path => fetch(base + path, {headers:{Authorization:`Bearer ${token}`}});

test.before(async () => {
  server = await new Promise(resolve => {
    const listener = app.listen(0, '127.0.0.1', () => resolve(listener));
  });
  base = `http://127.0.0.1:${server.address().port}/api/reports`;
});
test.after(async () => {
  await new Promise(resolve => server.close(resolve));
  await db.close();
});

test('daftar sepuluh laporan hanya dapat diakses admin', async () => {
  let response = await request('/types');
  assert.equal(response.status, 200);
  assert.equal((await response.json()).length, 10);
  role = 'downline';
  response = await request('/types');
  assert.equal(response.status, 403);
  role = 'admin';
});

test('filter tanggal divalidasi dan diteruskan ke query', async () => {
  assert.equal((await request('/deal-summary?from=2026-02-30')).status, 400);
  assert.equal((await request('/deal-summary?from=2026-02-02&to=2026-02-01')).status, 400);
  assert.equal((await request('/deal-summary?format=csv')).status, 400);
  assert.equal((await request('/unknown')).status, 404);
  rows = [{project:'P-1', deal_value:'125.50'}, {project:'P-2', deal_value:'74.50'}];
  const response = await request('/deal-summary?from=2026-01-01&to=2026-12-31');
  const body = await response.json();
  assert.deepEqual(body.summary, {deals:2, deal_value:200});
  assert.deepEqual(body.columns.map(column => column.label), ['Proyek','Klien','Agen','Tanggal Deal','Nilai Deal']);
  assert.equal(body.count, 2);
  assert.equal(lastQuery.options.replacements.from, '2026-01-01');
  assert.equal(lastQuery.options.replacements.to, '2026-12-31');
});

test('estimasi pipeline merinci setiap proyek dan ringkasannya', async () => {
  rows = [
    {project:'P-1', potential_revenue:'200', historic_deals:1, total_projects:4},
    {project:'P-2', potential_revenue:'100', historic_deals:1, total_projects:4}
  ];
  const body = await (await request('/revenue-forecast')).json();
  assert.equal(body.data[0].estimated_revenue, 50);
  assert.equal(body.data[1].historical_conversion_rate_percent, 25);
  assert.deepEqual(body.summary, {active_projects:2, potential_revenue:300, estimated_revenue:75});
  assert.ok(!('historic_deals' in body.data[0]));
});

test('ekspor Excel dan PDF memuat data dan ringkasan', async () => {
  rows = [{project:'=unsafe', deal_value:'125.50'}];
  let response = await request('/deal-summary?format=excel');
  assert.match(response.headers.get('content-type'), /spreadsheetml/);
  const workbook = new ExcelJS.Workbook();
  await workbook.xlsx.load(Buffer.from(await response.arrayBuffer()));
  const sheet = workbook.getWorksheet('deal-summary');
  assert.equal(sheet.getCell('A1').value, 'Proyek');
  assert.equal(sheet.getCell('E1').value, 'Nilai Deal');
  assert.equal(sheet.getCell('A1').font.bold, true);
  assert.equal(sheet.getCell('A1').fill.fgColor.argb, 'FF1946B8');
  assert.equal(sheet.getCell('A2').border.bottom.style, 'thin');
  assert.equal(sheet.getCell('A2').value, "'=unsafe");
  assert.equal(workbook.getWorksheet('Ringkasan').getCell('B3').value, 125.5);
  response = await request('/deal-summary?format=pdf');
  assert.match(response.headers.get('content-type'), /application\/pdf/);
  assert.equal(Buffer.from(await response.arrayBuffer()).subarray(0, 5).toString(), '%PDF-');
});

test('laporan kosong mempertahankan judul kolom', async () => {
  rows = [];
  let response = await request('/payment-history');
  const body = await response.json();
  assert.equal(body.count, 0);
  assert.equal(body.columns[0].label, 'Bukti Pembayaran');
  response = await request('/payment-history?format=excel');
  const workbook = new ExcelJS.Workbook();
  await workbook.xlsx.load(Buffer.from(await response.arrayBuffer()));
  assert.equal(workbook.getWorksheet('payment-history').getCell('A1').value, 'Bukti Pembayaran');
});

test('PDF tabel banyak halaman mengulang header dan tidak memotong isi panjang', async () => {
  rows = Array.from({length:80}, (_,i) => ({
    project:`PROYEK-${i}`, client:i === 0 ? 'X'.repeat(6000) + 'AKHIRDATA' : `Klien-${i}`,
    agent:'A-1', deal_date:'2026-01-01', deal_value:'100'
  }));
  const response = await request('/deal-summary?format=pdf');
  assert.equal(response.status, 200);
  const pdf = Buffer.from(await response.arrayBuffer());
  const raw = pdf.toString('latin1');
  assert.ok((raw.match(/\/Type\s*\/Page\b/g) || []).length > 2);
  const streams = [];
  const pattern = /stream\r?\n/g;
  for (const match of raw.matchAll(pattern)) {
    const end = raw.indexOf('endstream', match.index + match[0].length);
    if (end < 0) continue;
    try { streams.push(zlib.inflateSync(pdf.subarray(match.index + match[0].length, end)).toString('latin1')); }
    catch (error) { if (error.code !== 'Z_DATA_ERROR') throw error; }
  }
  const decoded = streams.map(stream => [...stream.matchAll(/<([0-9a-f]+)>/gi)]
    .map(match => Buffer.from(match[1], 'hex').toString('latin1')).join('')).join('');
  assert.ok(decoded.includes('AKHIRDATA'));
  assert.ok((decoded.match(/Proyek/g) || []).length > 2);
});
