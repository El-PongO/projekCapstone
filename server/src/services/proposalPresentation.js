'use strict';

const {num,round} = require('../utils/common');
const {drawPdfTable} = require('./reportPresentation');

function drawProposal(pdf, {title, number, projectCode, clientName, validUntil, createdAt, lines}) {
  const money = value => `Rp ${num(value).toLocaleString('id-ID', {minimumFractionDigits:0, maximumFractionDigits:2})}`;
  const rows = lines.map((line, index) => ({
    number:index + 1,
    item:line.item_name,
    description:line.description || '-',
    quantity:line.quantity,
    price:money(line.unit_price),
    subtotal:money(round(num(line.unit_price) * line.quantity))
  }));
  const total = lines.reduce((sum,line) => round(sum + round(num(line.unit_price) * line.quantity)),0);

  pdf.font('Helvetica-Bold').fontSize(18).fillColor('#1946B8').text('INTEGRA AGENCY').moveDown(0.3);
  pdf.font('Helvetica-Bold').fontSize(15).fillColor('#1E293B').text(title).moveDown(0.6);
  pdf.font('Helvetica').fontSize(10).fillColor('#475569')
    .text(`Nomor: ${number}`).text(`Proyek: ${projectCode}`).text(`Klien: ${clientName}`)
    .text(`Tanggal: ${createdAt.toLocaleDateString('id-ID')}`)
    .text(`Berlaku sampai: ${validUntil || '-'}`).moveDown();
  pdf.font('Helvetica-Bold').fontSize(11).fillColor('#1E293B').text('Offer Details').moveDown(0.5);
  drawPdfTable(pdf, [
    {key:'number',label:'No.',weight:0.4},
    {key:'item',label:'Item',weight:1.3},
    {key:'description',label:'Deskripsi',weight:2},
    {key:'quantity',label:'Jumlah',weight:0.6},
    {key:'price',label:'Harga Satuan',weight:1.1},
    {key:'subtotal',label:'Subtotal',weight:1.1}
  ], rows);
  pdf.moveDown().font('Helvetica-Bold').fontSize(13).fillColor('#1946B8')
    .text(`TOTAL PENAWARAN: ${money(total)}`, {align:'right'});
  pdf.moveDown(2).font('Helvetica').fontSize(9).fillColor('#475569')
    .text('Dokumen dibuat otomatis oleh Integra Agency.');
  return total;
}

module.exports = {drawProposal};
