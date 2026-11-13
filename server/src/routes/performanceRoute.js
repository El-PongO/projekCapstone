
const express = require("express");
const router = express.Router();

const performanceController = require(
    "../controllers/performanceController"
);

const requireAuth = require(
    "../middleware/authMiddleware"
);

// GET /api/performance
router.get(
    "/",
    requireAuth,
    performanceController.getPerformance
);

module.exports = router;
