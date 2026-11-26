'use strict';

const reports = {
  'agent-performance': ['Performa & Kinerja Agen', ['agent','role','total_projects','deals','conversion_percent','deal_value']],
  'commission-sharing': ['Komisi & Bagi Hasil', ['project','deal_date','deal_value','recipient','agent_type','rate_percent','share_amount','payout_status']],
  'project-conversion': ['Konversi Proyek', ['agent','total_projects','follow_up','deals','no_deal','cancelled','conversion_percent']],
  'deal-summary': ['Ringkasan Deal & Nilai Proyek', ['project','client','agent','deal_date','deal_value']],
  'payment-history': ['Pembayaran & Riwayat Transaksi', ['receipt','invoice','project','amount_paid','payment_date','payment_method']],
  'aging-payment': ['Piutang & Umur Pembayaran', ['invoice','project','due_date','total_amount','paid_amount','outstanding','overdue_days','aging_bucket']],
  'cancel-analysis': ['Evaluasi Proyek Batal', ['project','client','agent','status','reason','updated_at']],
  'agent-hierarchy': ['Produktivitas Hierarki Agen', ['upline','downline','joined_at','projects','deals','downline_deal_value','downline_commission']],
  'project-category': ['Portofolio Kategori Proyek', ['category','project_count','deal_count','line_value']],
  'revenue-forecast': ['Proyeksi Potensi Pendapatan', ['project','client','agent','stage','potential_revenue','historical_conversion_rate_percent','estimated_revenue','note']]
};
const labels = {
  agent:'Agen', role:'Peran', total_projects:'Total Proyek', deals:'Deal',
  conversion_percent:'Konversi (%)', deal_value:'Nilai Deal', project:'Proyek',
  deal_date:'Tanggal Deal', recipient:'Penerima', agent_type:'Jenis Agen',
  rate_percent:'Rate (%)', share_amount:'Nilai Bagi Hasil', payout_status:'Status Pembayaran Komisi',
  follow_up:'Follow Up', no_deal:'No Deal', cancelled:'Batal', client:'Klien',
  receipt:'Bukti Pembayaran', invoice:'Invoice', amount_paid:'Jumlah Dibayar',
  payment_date:'Tanggal Pembayaran', payment_method:'Metode Pembayaran', due_date:'Jatuh Tempo',
  total_amount:'Total Tagihan', paid_amount:'Sudah Dibayar', outstanding:'Sisa Tagihan',
  overdue_days:'Hari Terlambat', aging_bucket:'Umur Pembayaran', status:'Status',
  reason:'Alasan', updated_at:'Terakhir Diperbarui', upline:'Upline', downline:'Downline',
  joined_at:'Tanggal Bergabung', projects:'Proyek', downline_deal_value:'Nilai Deal Downline',
  downline_commission:'Komisi Downline', category:'Kategori', project_count:'Jumlah Proyek',
  deal_count:'Jumlah Deal', line_value:'Nilai Item Deal', stage:'Tahap',
  potential_revenue:'Potensi Pendapatan', historical_conversion_rate_percent:'Konversi Historis (%)',
  estimated_revenue:'Estimasi Pendapatan', note:'Catatan', active_projects:'Proyek Aktif',
  payments:'Jumlah Pembayaran', invoices:'Jumlah Invoice'
};
const label = key => labels[key] || key.replace(/_/g, ' ');
const columns = type => reports[type][1].map(key => ({key, label:label(key)}));
const valueText = value => value == null ? '-' : value instanceof Date
  ? value.toISOString().replace('T',' ').slice(0,19) : String(value);
const excelValue = value => {
  if (value == null) return '';
  if (value instanceof Date) return valueText(value);
  return typeof value === 'string' && /^[\s]*[=+@-]/.test(value) ? "'" + value : value;
};

function styleSheet(ws, columnCount) {
  const border = {style:'thin', color:{argb:'FFD7E1EF'}};
  ws.eachRow(row => row.eachCell({includeEmpty:true}, cell => {
    cell.border = {top:border, bottom:border, left:border, right:border};
    cell.alignment = {vertical:'middle', wrapText:true};
    if (row.number === 1) {
      cell.font = {bold:true, color:{argb:'FFFFFFFF'}};
      cell.fill = {type:'pattern', pattern:'solid', fgColor:{argb:'FF1946B8'}};
    } else if (row.number % 2 === 0) {
      cell.fill = {type:'pattern', pattern:'solid', fgColor:{argb:'FFF3F6FB'}};
    }
  }));
  ws.getRow(1).height = 30;
  ws.views = [{state:'frozen', ySplit:1}];
  ws.autoFilter = {from:{row:1,column:1}, to:{row:1,column:columnCount}};
  ws.columns = Array.from({length:columnCount}, () => ({width:24}));
}

function addTableSheet(wb, name, headers, rows) {
  const ws = wb.addWorksheet(name);
  ws.addRow(headers);
  rows.forEach(row => ws.addRow(row.map(excelValue)));
  styleSheet(ws, headers.length);
}

function wrapText(pdf, value, width) {
  const lines = [];
  for (const paragraph of valueText(value).split(/\r?\n/)) {
    let line = '';
    for (const char of paragraph) {
      if (line && pdf.widthOfString(line + char) > width) {
        lines.push(line);
        line = char;
      } else line += char;
    }
    lines.push(line);
  }
  return lines;
}

function drawPdfReport(pdf, type, from, to, data, summary) {
  pdf.font('Helvetica-Bold').fontSize(16).fillColor('#1946B8').text(reports[type][0]);
  pdf.font('Helvetica').fontSize(9).fillColor('#475569')
    .text(`Periode: ${from === '1900-01-01' ? 'Awal' : from} - ${to === '9999-12-31' ? 'Sekarang' : to}`).moveDown();
  for (const [key,value] of Object.entries(summary)) {
    pdf.font('Helvetica-Bold').fillColor('#1E293B').text(`${label(key)}: ${valueText(value)}`);
  }
  pdf.moveDown();
  drawPdfTable(pdf, columns(type), data);
}

function drawPdfTable(pdf, cols, data) {
  const left = pdf.page.margins.left;
  const width = pdf.page.width - left - pdf.page.margins.right;
  const weights = cols.map(col => col.weight || 1);
  const weightTotal = weights.reduce((sum, weight) => sum + weight, 0);
  const widths = weights.map(weight => width * weight / weightTotal);
  const positions = widths.map((_, i) => left + widths.slice(0, i).reduce((sum, w) => sum + w, 0));
  const bottom = () => pdf.page.height - pdf.page.margins.bottom;
  const padding = 4;
  const lineHeight = 10;
  let y = pdf.y;
  function header() {
    pdf.font('Helvetica-Bold').fontSize(8);
    const lines = cols.map((col,i) => wrapText(pdf, col.label, widths[i] - 2*padding));
    const height = Math.max(...lines.map(x => x.length))*lineHeight + 2*padding;
    if (y + height + lineHeight + 2*padding > bottom()) {
      pdf.addPage();
      y = pdf.page.margins.top;
    }
    pdf.rect(left,y,width,height).fill('#1946B8');
    lines.forEach((parts,i) => parts.forEach((part,j) => {
      pdf.fillColor('#FFFFFF').text(part,positions[i]+padding,y+padding+j*lineHeight,{lineBreak:false});
    }));
    y += height;
  }
  function nextPage() { pdf.addPage(); y = pdf.page.margins.top; header(); }
  header();
  pdf.font('Helvetica').fontSize(8);
  for (const row of data) {
    pdf.font('Helvetica').fontSize(8);
    const cells = cols.map((col,i) => wrapText(pdf,row[col.key],widths[i]-2*padding));
    const lines = Math.max(...cells.map(cell => cell.length));
    let offset = 0;
    while (offset < lines) {
      let available = Math.floor((bottom()-y-2*padding)/lineHeight);
      if (available < 1) { nextPage(); available = Math.floor((bottom()-y-2*padding)/lineHeight); }
      const count = Math.min(available,lines-offset);
      const height = count*lineHeight+2*padding;
      pdf.font('Helvetica').fontSize(8);
      cols.forEach((col,i) => {
        const x = positions[i];
        pdf.rect(x,y,widths[i],height).lineWidth(0.5).strokeColor('#CBD5E1').stroke();
        for (let j=0; j<count; j++) {
          if (cells[i][offset+j] !== undefined)
            pdf.fillColor('#1E293B').text(cells[i][offset+j],x+padding,y+padding+j*lineHeight,{lineBreak:false});
        }
      });
      y += height;
      offset += count;
      if (offset < lines) nextPage();
    }
  }
  pdf.x = left;
  pdf.y = y;
  if (!data.length) pdf.font('Helvetica').fontSize(9).fillColor('#475569').text('Belum ada data',left,y+10);
}

module.exports = {reports, columns, label, addTableSheet, drawPdfReport, drawPdfTable};
