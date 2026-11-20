'use strict';
// Perhitungan tanggal DATEONLY menggunakan kalender, bukan jam setempat.
function daysBetweenIso(today, due) {
  if (typeof today !== 'string' || typeof due !== 'string' ||
      !/^\d{4}-\d{2}-\d{2}$/.test(today) || !/^\d{4}-\d{2}-\d{2}$/.test(due)) {
    throw new Error('Tanggal harus YYYY-MM-DD');
  }
  const a = Date.parse(today + 'T00:00:00Z');
  const b = Date.parse(due + 'T00:00:00Z');
  if (!Number.isFinite(a) || !Number.isFinite(b) ||
      new Date(a).toISOString().slice(0, 10) !== today ||
      new Date(b).toISOString().slice(0, 10) !== due) throw new Error('Tanggal tidak valid');
  return Math.round((b - a) / 86400000);
}
function invoiceState(invoice, today, paidAmount = 0, noticeDays = 3) {
  const due = String(invoice.due_date || '').slice(0, 10);
  if (!due) return { state: 'no_due_date', overdueDays: 0, remaining: 0 };
  const total = Math.max(0, Number(invoice.total_amount || 0));
  const paid = Math.max(0, Number(paidAmount || 0));
  const remaining = Math.round(Math.max(0, total - paid) * 100) / 100;
  if (invoice.status === 'paid' || remaining <= 0) return { state: 'paid', overdueDays: 0, remaining: 0 };
  const difference = daysBetweenIso(today, due);
  if (difference < 0) return { state: 'overdue', overdueDays: -difference, remaining, dueDate: due };
  if (difference <= noticeDays) return { state: 'due_soon', daysRemaining: difference, overdueDays: 0, remaining, dueDate: due };
  return { state: 'ongoing', daysRemaining: difference, overdueDays: 0, remaining, dueDate: due };
}
function agingBucket(overdueDays) {
  const days = Number(overdueDays || 0);
  return days <= 0 ? 'Belum jatuh tempo' : days <= 30 ? '1-30 hari' : days <= 60 ? '31-60 hari' : '>60 hari';
}
module.exports = { daysBetweenIso, invoiceState, agingBucket };
