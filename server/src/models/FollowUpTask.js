const { DataTypes } = require("sequelize");
const sequelize = require("../config/database");
module.exports = sequelize.define("FollowUpTask", {
  id: { type: DataTypes.BIGINT.UNSIGNED, primaryKey: true, autoIncrement: true },
  project_id: DataTypes.BIGINT.UNSIGNED,
  agent_id: DataTypes.BIGINT.UNSIGNED,
  title: DataTypes.STRING(255),
  due_at: DataTypes.DATE,
  status: DataTypes.ENUM("pending","completed","cancelled"),
  notes: DataTypes.TEXT,
  created_at: DataTypes.DATE
}, { tableName: "follow_up_tasks", timestamps: false, freezeTableName: true });
