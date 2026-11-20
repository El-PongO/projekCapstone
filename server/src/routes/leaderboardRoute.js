'use strict';
const r = require('express').Router();
const db = require('../config/database');
const { QueryTypes } = require('sequelize');
const { auth } = require('../middleware/auth');
const { wrap, date, fail } = require('../utils/common');
r.use(auth);
// Hanya kode agen; nama, email, telepon, user_id dan identitas klien tidak dikirim.
r.get('/', wrap(async (req, res) => {
  const from = req.query.from ? date(req.query.from) : '1900-01-01';
  const to = req.query.to ? date(req.query.to) : '9999-12-31';
  if (from > to) throw fail(400, 'from tidak boleh setelah to');
  const limit = Math.min(100, Math.max(1, Number.parseInt(req.query.limit, 10) || 20));
  const rows = await db.query(`
    SELECT u.agent_code AS agentCode,
           COUNT(p.id) AS totalDeals,
           COALESCE(SUM(p.deal_value), 0) AS totalDealValue
      FROM users u
      INNER JOIN projects p ON p.agent_id = u.id AND p.status = 'deal'
           AND COALESCE(p.deal_date, DATE(p.updated_at)) BETWEEN :from AND :to
     WHERE u.role IN ('upline','downline') AND u.is_active = 1
     GROUP BY u.id, u.agent_code
     ORDER BY totalDealValue DESC, totalDeals DESC, u.agent_code ASC
     LIMIT :limit`, {
    replacements: { from, to, limit }, type: QueryTypes.SELECT
  });
  res.json({ period: { from, to }, ranking: rows.map((x, i) => ({
    rank: i + 1, agentCode: x.agentCode,
    totalDeals: Number(x.totalDeals), totalDealValue: Number(x.totalDealValue)
  })) });
}));
module.exports = r;
