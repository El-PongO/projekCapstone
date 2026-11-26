// The seed users have placeholder (non-bcrypt) password values, so nobody can log in.
// This sets a real bcrypt hash for the seeded demo accounts. Run once: npm run seed-passwords
require('dotenv').config();
const bcrypt = require('bcryptjs');
const pool = require('../src/db');

const PASSWORD = process.argv[2] || 'password123';

(async () => {
  const hash = await bcrypt.hash(PASSWORD, 10);
  const [r] = await pool.execute('UPDATE users SET password = ?', [hash]);
  console.log(`Updated ${r.affectedRows} users. Password for all: ${PASSWORD}`);
  await pool.end();
})().catch((e) => {
  console.error(e.message);
  process.exit(1);
});
