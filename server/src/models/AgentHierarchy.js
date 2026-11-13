const { DataTypes } = require("sequelize");
const sequelize = require("../config/database");

const AgentHierarchy = sequelize.define("AgentHierarchy", {
  id: { type: DataTypes.BIGINT.UNSIGNED, primaryKey: true, autoIncrement: true },
  upline_id: { type: DataTypes.BIGINT.UNSIGNED, allowNull: false },
  downline_id: { type: DataTypes.BIGINT.UNSIGNED, allowNull: false },
  depth: { type: DataTypes.INTEGER, allowNull: false }
}, {
  tableName: "agent_hierarchies",
  timestamps: false
});

module.exports = AgentHierarchy;
