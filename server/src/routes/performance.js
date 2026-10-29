const router = require('express').Router();
const pool = require('../db');
const requireAuth = require('../middleware/auth');

// GET /api/performance  -> the logged-in agent's own sales report and income
router.get('/', requireAuth, async (req, res) => {
  try {
    const [sales] = await pool.execute(
      `SELECT project_code, client_name, status, execution_stage, deal_value, created_at
         FROM projects WHERE agent_id = ? ORDER BY created_at DESC, id DESC`,
      [req.user.id]
    );
    const [income] = await pool.execute(
      `SELECT p.project_code, p.client_name, p.deal_value, p.calculated_commission,
              u.rate_value_percent, p.updated_at
         FROM projects p JOIN users u ON u.id = p.agent_id
        WHERE p.agent_id = ? AND p.status = 'deal'
        ORDER BY p.updated_at DESC, p.id DESC`,
      [req.user.id]
    );

    const totalSales = sales.filter((s) => s.status === 'deal').reduce((sum, s) => sum + Number(s.deal_value), 0);
    const totalIncome = income.reduce((sum, i) => sum + Number(i.calculated_commission), 0);

    res.json({
      stats: { totalSales, totalIncome },
      sales: sales.map((s) => ({
        projectCode: s.project_code,
        clientName: s.client_name,
        status: s.status,
        stage: s.execution_stage,
        dealValue: Number(s.deal_value),
        date: s.created_at,
      })),
      income: income.map((i) => ({
        projectCode: i.project_code,
        clientName: i.client_name,
        dealValue: Number(i.deal_value),
        commission: Number(i.calculated_commission),
        ratePercent: Number(i.rate_value_percent),
        date: i.updated_at,
      })),
    });
  } catch (e) {
    console.error(e);
    res.status(500).json({ error: 'Server error' });
  }
});

module.exports = router;
