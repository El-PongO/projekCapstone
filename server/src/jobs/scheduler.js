'use strict';
const { run } = require('./reminders');
let running = false;
function startReminderScheduler() {
  if (process.env.ENABLE_REMINDERS === 'false') return;
  async function tick() {
    if (running) return;
    running = true;
    try { await run(); } catch (error) { console.error('[Reminder Scheduler]', error); }
    finally { running = false; }
  }
  // Pertama saat server sudah berjalan; selanjutnya setiap 6 jam.
  setTimeout(tick, 30 * 1000).unref();
  setInterval(tick, 6 * 60 * 60 * 1000).unref();
}
module.exports = { startReminderScheduler };
