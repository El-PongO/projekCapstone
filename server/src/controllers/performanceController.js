
const Project = require("../models/Project");
const User = require("../models/User");

// Mengambil tahun dan bulan sesuai WIB
function getCurrentMonth() {
    const parts = new Intl.DateTimeFormat("en-US", {
        timeZone: "Asia/Jakarta",
        year: "numeric",
        month: "2-digit"
    }).formatToParts(new Date());

    const year = parts.find(p => p.type === "year").value;
    const month = parts.find(p => p.type === "month").value;

    return `${year}-${month}`;
}

// GET /api/performance
const getPerformance = async (req, res) => {
    try {
        // ID berasal dari JWT yang sudah diverifikasi.
        const userId = req.user.id;

        // Ambil data pengguna
        const user = await User.findByPk(userId);

        if (!user || !user.is_active) {
            return res.status(401).json({
                error: "Akun tidak ditemukan atau tidak aktif"
            });
        }

        // Ambil proyek milik agen yang login saja
        const projects = await Project.findAll({
            where: {
                agent_id: userId
            },
            order: [
                ["created_at", "DESC"],
                ["id", "DESC"]
            ]
        });

        // ======================================
        // 1. MY SALES REPORT
        // ======================================

        const sales = projects.map(project => ({
            projectCode: project.project_code,
            clientName: project.client_name,
            status: project.status,
            stage: project.execution_stage,
            dealValue: Number(project.deal_value || 0),
            date: project.createdAt
        }));

        // ======================================
        // 2. PROJECT DEAL
        // ======================================

        const dealProjects = projects.filter(
            project => project.status === "deal"
        );

        const totalDealsMade = dealProjects.length;

        // ======================================
        // 3. MY INCOME
        // ======================================

        const income = dealProjects.map(project => ({
            projectCode: project.project_code,
            clientName: project.client_name,
            dealValue: Number(project.deal_value || 0),
            commission: Number(
                project.calculated_commission || 0
            ),
            ratePercent: Number(user.rate_value_percent || 0),
            date: project.deal_date || project.updatedAt
        }));

        // ======================================
        // 4. TOTAL SALES
        // ======================================

        const totalSales = dealProjects.reduce(
            (total, project) =>
                total + Number(project.deal_value || 0),
            0
        );

        // ======================================
        // 5. TOTAL INCOME
        // ======================================

        const totalIncome = dealProjects.reduce(
            (total, project) =>
                total + Number(project.calculated_commission || 0),
            0
        );

        // ======================================
        // 6. THIS MONTH'S CUT
        // ======================================

        const currentMonth = getCurrentMonth();

        const monthlyProjects = dealProjects.filter(project => {
            if (!project.deal_date) return false;

            return String(project.deal_date)
                .startsWith(currentMonth);
        });

        const monthlyCut = monthlyProjects.reduce(
            (total, project) =>
                total + Number(project.calculated_commission || 0),
            0
        );

        // ======================================
        // 7. RESPONSE
        // ======================================

        return res.status(200).json({
            stats: {
                totalSales,
                totalIncome,
                totalDealsMade,
                monthlyCut
            },
            sales,
            income
        });

    } catch (error) {
        console.error("Get Performance Error:", error);

        return res.status(500).json({
            error: "Gagal mengambil data My Performance"
        });
    }
};

module.exports = {
    getPerformance
};
