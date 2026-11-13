const { Op } = require("sequelize");
const User = require("../models/User");
const Project = require("../models/Project");
const AgentHierarchy = require("../models/AgentHierarchy");

const getDashboard = async (req, res) => {
  try {
    const userId = req.user.id;
    const role = req.user.role;
    if (!["admin", "upline", "downline"].includes(role)) {
      return res.status(403).json({ error: "Tidak memiliki akses" });
    }

    // Tentukan proyek yang boleh dilihat berdasarkan role (bukan dari request Android).
    let where = {};
    let agentCount = 0;

    if (role === "admin") {
      agentCount = await User.count({ where: { role: { [Op.ne]: "admin" }, is_active: true } });
    } else if (role === "upline") {
      const hierarchies = await AgentHierarchy.findAll({ where: { upline_id: userId } });
      const downlineIds = hierarchies.map(item => Number(item.downline_id));
      where = { agent_id: { [Op.in]: [userId, ...downlineIds] } };
      agentCount = hierarchies.length;
    } else {
      where = { agent_id: userId };
    }

    const totalProjects = await Project.count({ where });
    const dealCount = await Project.count({ where: { ...where, status: "deal" } });
    const followUpCount = await Project.count({ where: { ...where, status: "follow_up" } });
    const dealValue = await Project.sum("deal_value", { where: { ...where, status: "deal" } });
    const myCommission = await Project.sum("calculated_commission", {
      where: { agent_id: userId, status: "deal" }
    });

    const projects = await Project.findAll({
      where,
      order: [["updated_at", "DESC"], ["id", "DESC"]],
      limit: 5
    });
    const agentIds = [...new Set(projects.map(item => Number(item.agent_id)))];
    const agents = agentIds.length > 0 ? await User.findAll({
      where: { id: { [Op.in]: agentIds } }, attributes: ["id", "agent_code"]
    }) : [];
    const codeById = new Map(agents.map(user => [Number(user.id), user.agent_code]));

    res.json({
      role,
      stats: {
        totalProjects,
        dealCount,
        followUpCount,
        dealValue: Number(dealValue || 0),
        agentCount,
        myCommission: Number(myCommission || 0)
      },
      recentProjects: projects.map(project => {
        const agentId = Number(project.agent_id);
        const agentCode = codeById.get(agentId) || "";
        return {
          projectCode: project.project_code,
          title: role === "upline" && agentId !== userId
            ? `Proyek ${agentCode}` : project.client_name,
          agentCode,
          status: project.status,
          stage: project.execution_stage,
          dealValue: Number(project.deal_value)
        };
      })
    });
  } catch (err) {
    console.error(err);
    res.status(500).json({ error: "Gagal memuat dashboard" });
  }
};

module.exports = { getDashboard };
