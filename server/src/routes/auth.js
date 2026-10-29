const router = require('express').Router();
const bcrypt = require('bcryptjs');
const jwt = require('jsonwebtoken');
const pool = require('../db');

// POST /api/auth/login  { identifier: email | agent_code, password }
router.post('/login', async (req, res) => {
  const { identifier, password } = req.body || {};
  if (!identifier || !password) {
    return res.status(400).json({ error: 'Identifier and password are required' });
  }
  try {
    const [rows] = await pool.execute(
      'SELECT id, agent_code, name, email, password, role, is_active FROM users WHERE email = ? OR agent_code = ? LIMIT 1',
      [identifier.trim(), identifier.trim()]
    );
    const user = rows[0];
    const ok = user && user.is_active && (await bcrypt.compare(password, user.password));
    if (!ok) return res.status(401).json({ error: 'Invalid username or password' });

    const token = jwt.sign({ sub: user.id, role: user.role }, process.env.JWT_SECRET, { expiresIn: '7d' });
    res.json({
      token,
      user: { id: user.id, agentCode: user.agent_code, name: user.name, email: user.email, role: user.role },
    });
  } catch (e) {
    console.error(e);
    res.status(500).json({ error: 'Server error' });
  }
});

module.exports = router;
