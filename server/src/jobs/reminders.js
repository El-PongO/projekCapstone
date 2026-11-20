'use strict';
require('dotenv').config();
const { Op } = require('sequelize');
const db = require('../config/database');
const { FollowUpTask, Meeting, Notification, Invoice, Payment, User } = require('../models');
const { notify, push, initFirebase } = require('../services/notify');
const { dateWib } = require('../utils/common');
const { invoiceState } = require('../services/capstoneFinance');

// Jalankan per hari lewat Windows Task Scheduler / cron: npm run remind.
async function run() {
  await db.authenticate();
  initFirebase();
  const now = new Date();
  const next = new Date(now.getTime() + 24 * 3600000);
  const since = new Date(now.getTime() - 24 * 3600000);
  let sent = 0;
  async function sendOnce(userId, title, message, type = 'system') {
    const existing = await Notification.findOne({ where: { user_id: userId, title, message, created_at: { [Op.gte]: since } } });
    if (existing) return false;
    await notify(userId, title, message, type);
    await push(userId, title, message);
    sent++;
    return true;
  }
  const tasks = await FollowUpTask.findAll({ where: { status: 'pending', due_at: { [Op.between]: [now, next] } } });
  for (const t of tasks) {
    await sendOnce(t.agent_id, 'Pengingat Follow Up', `Follow-up #${t.id}: ${t.title} (jadwal ${new Date(t.due_at).toISOString()})`);
  }
  const meetings = await Meeting.findAll({ where: { status: 'scheduled', meeting_at: { [Op.between]: [now, next] } } });
  for (const m of meetings) {
    await sendOnce(m.agent_id, 'Pengingat Meeting', `Meeting #${m.id}: ${m.meeting_title} (jadwal ${new Date(m.meeting_at).toISOString()})`);
  }
  // Tagihan jatuh tempo: dihitung dari tanggal WIB (DATEONLY), bukan status UI.
  const noticeDays = Math.max(0, Math.min(30, Number.parseInt(process.env.INVOICE_REMINDER_DAYS || '3', 10) || 0));
  const today = dateWib(now);
  const [invoices, admins] = await Promise.all([
    Invoice.findAll({ where: { status: { [Op.in]: ['unpaid','partial'] } } }),
    User.findAll({ where: { role: 'admin', is_active: true }, attributes: ['id'] })
  ]);
  if (invoices.length && admins.length) {
    const payments = await Payment.findAll({ where: { invoice_id: { [Op.in]: invoices.map(x => x.id) } }, attributes: ['invoice_id','amount_paid'] });
    const paidByInvoice = new Map();
    for (const p of payments) paidByInvoice.set(Number(p.invoice_id), (paidByInvoice.get(Number(p.invoice_id)) || 0) + Number(p.amount_paid || 0));
    for (const invoice of invoices) {
      const state = invoiceState(invoice, today, paidByInvoice.get(Number(invoice.id)) || 0, noticeDays);
      if (state.state !== 'overdue' && state.state !== 'due_soon') continue;
      const title = state.state === 'overdue' ? 'Invoice Lewat Jatuh Tempo' : 'Invoice Mendekati Jatuh Tempo';
      const message = `Invoice ${invoice.invoice_number} (proyek #${invoice.project_id}), jatuh tempo ${state.dueDate}, sisa tagihan ${state.remaining.toFixed(2)}`;
      for (const admin of admins) await sendOnce(admin.id, title, message);
    }
  }
  console.log(`Pengingat baru: ${sent}`);
}
if (require.main === module) run().catch(e => { console.error('Reminder gagal:', e); process.exitCode = 1; }).finally(() => db.close());
module.exports = { run };
