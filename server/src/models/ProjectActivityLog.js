const { DataTypes } = require("sequelize");
const sequelize = require("../config/database");
module.exports = sequelize.define("ProjectActivityLog", {
  id: { type: DataTypes.BIGINT.UNSIGNED, primaryKey: true, autoIncrement: true },
  project_id: DataTypes.BIGINT.UNSIGNED,
  agent_id: DataTypes.BIGINT.UNSIGNED,
  activity_description: DataTypes.TEXT,
  previous_stage: DataTypes.STRING(50),
  current_stage: DataTypes.STRING(50),
  created_at: DataTypes.DATE
}, { tableName: "project_activity_logs", timestamps: false, freezeTableName: true });
