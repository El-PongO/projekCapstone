const { DataTypes } = require("sequelize");
const sequelize = require("../config/database");
module.exports = sequelize.define("AgentHierarchy", {
  id: { type: DataTypes.BIGINT.UNSIGNED, primaryKey: true, autoIncrement: true },
  upline_id: DataTypes.BIGINT.UNSIGNED,
  downline_id: DataTypes.BIGINT.UNSIGNED,
  depth: DataTypes.INTEGER,
  created_at: DataTypes.DATE
}, { tableName: "agent_hierarchies", timestamps: false, freezeTableName: true });
