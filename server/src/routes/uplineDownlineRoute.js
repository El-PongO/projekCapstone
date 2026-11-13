
const express = require("express");
const jwt = require("jsonwebtoken");
const { QueryTypes } = require("sequelize");
const database = require("../config/database");

const router = express.Router();
const sequelize = database.sequelize || database;

// ======================================
// MIDDLEWARE: HANYA UPLINE
// ======================================

async function onlyUpline(req, res, next) {
    try {
        const authorization = req.headers.authorization || "";

        if (!authorization.startsWith("Bearer ")) {
            return res.status(401).json({
                message: "Token tidak ditemukan"
            });
        }

        const token = authorization.substring(7);
        const payload = jwt.verify(token, process.env.JWT_SECRET);

        // Mendukung beberapa format JWT yang umum.
        const userId = Number(
            payload.userId ??
            payload.user_id ??
            payload.id ??
            payload.sub ??
            payload.user?.id
        );

        if (!Number.isSafeInteger(userId) || userId <= 0) {
            return res.status(401).json({
                message: "Format token tidak valid"
            });
        }

        // Role diverifikasi dari database, bukan dari input Android.
        const users = await sequelize.query(
            `SELECT id, role, is_active
             FROM users
             WHERE id = :userId
             LIMIT 1`,
            {
                replacements: { userId },
                type: QueryTypes.SELECT
            }
        );

        const user = users[0];

        if (!user || !user.is_active) {
            return res.status(401).json({
                message: "Akun tidak aktif atau tidak ditemukan"
            });
        }

        if (user.role !== "upline") {
            return res.status(403).json({
                message: "Hanya Upline yang dapat mengakses halaman ini"
            });
        }

        req.uplineId = userId;
        next();

    } catch (error) {
        if (error.name === "JsonWebTokenError" ||
            error.name === "TokenExpiredError") {
            return res.status(401).json({
                message: "Token tidak valid atau kedaluwarsa"
            });
        }

        console.error("Upline auth error:", error);

        return res.status(500).json({
            message: "Terjadi kesalahan server"
        });
    }
}

router.use(onlyUpline);

// ======================================
// GET /api/upline/downlines
// Menampilkan seluruh Downline Upline
// ======================================

router.get("/", async (req, res) => {
    try {
        const rows = await sequelize.query(
            `
            SELECT
                d.id,
                d.agent_code AS agentCode,
                h.created_at AS joinedAt,
                COUNT(p.id) AS totalDeals,

                ROUND(
                    COUNT(p.id) /
                    GREATEST(
                        TIMESTAMPDIFF(
                            MONTH,
                            h.created_at,
                            CURRENT_DATE()
                        ) + 1,
                        1
                    ),
                    1
                ) AS dealsPerMonth

            FROM agent_hierarchies h

            INNER JOIN users d
                ON d.id = h.downline_id

            LEFT JOIN projects p
                ON p.agent_id = d.id
                AND p.status = 'deal'

            WHERE h.upline_id = :uplineId
                AND d.role = 'downline'
                AND d.is_active = 1

            GROUP BY
                d.id,
                d.agent_code,
                h.created_at

            ORDER BY d.agent_code ASC
            `,
            {
                replacements: {
                    uplineId: req.uplineId
                },
                type: QueryTypes.SELECT
            }
        );

        const downlines = rows.map(row => ({
            id: Number(row.id),
            agentCode: row.agentCode,
            joinedAt: row.joinedAt,
            totalDeals: Number(row.totalDeals),
            dealsPerMonth: Number(row.dealsPerMonth)
        }));

        const totalDeals = downlines.reduce(
            (total, item) => total + item.totalDeals,
            0
        );

        return res.json({
            summary: {
                downlineCount: downlines.length,
                totalDeals
            },
            downlines
        });

    } catch (error) {
        console.error("Get downlines error:", error);

        return res.status(500).json({
            message: "Gagal mengambil data Downline"
        });
    }
});

// ======================================
// GET /api/upline/downlines/:downlineId
// Menampilkan detail performa Downline
// ======================================

router.get("/:downlineId", async (req, res) => {
    try {
        const downlineId = Number(req.params.downlineId);

        if (!Number.isSafeInteger(downlineId) || downlineId <= 0) {
            return res.status(400).json({
                message: "ID Downline tidak valid"
            });
        }

        // Pastikan Downline memang berada di bawah Upline login.
        const downlines = await sequelize.query(
            `
            SELECT
                d.id,
                d.agent_code AS agentCode,
                h.created_at AS joinedAt

            FROM agent_hierarchies h
            INNER JOIN users d
                ON d.id = h.downline_id

            WHERE h.upline_id = :uplineId
              AND h.downline_id = :downlineId
              AND d.role = 'downline'
              AND d.is_active = 1

            LIMIT 1
            `,
            {
                replacements: {
                    uplineId: req.uplineId,
                    downlineId
                },
                type: QueryTypes.SELECT
            }
        );

        if (downlines.length === 0) {
            return res.status(404).json({
                message: "Downline tidak ditemukan"
            });
        }

        const projects = await sequelize.query(
            `
            SELECT
                id,
                project_code AS code,
                status,
                execution_stage AS stage,
                deal_value AS dealValue,
                updated_at AS updatedAt

            FROM projects

            WHERE agent_id = :downlineId
            ORDER BY updated_at DESC
            `,
            {
                replacements: { downlineId },
                type: QueryTypes.SELECT
            }
        );

        const totalDeals = projects.filter(
            project => project.status === "deal"
        ).length;

        const joinedAt = new Date(downlines[0].joinedAt);
        const now = new Date();

        const months = Math.max(
            1,
            (now.getFullYear() - joinedAt.getFullYear()) * 12 +
            (now.getMonth() - joinedAt.getMonth()) + 1
        );

        return res.json({
            downline: {
                id: Number(downlines[0].id),
                agentCode: downlines[0].agentCode,
                joinedAt: downlines[0].joinedAt,
                totalDeals,
                dealsPerMonth: Number(
                    (totalDeals / months).toFixed(1)
                )
            },
            projects: projects.map(project => ({
                id: Number(project.id),
                code: project.code,
                status: project.status,
                stage: project.stage,
                dealValue: Number(project.dealValue || 0),
                updatedAt: project.updatedAt
            }))
        });

    } catch (error) {
        console.error("Get downline detail error:", error);

        return res.status(500).json({
            message: "Gagal mengambil detail Downline"
        });
    }
});

module.exports = router;
