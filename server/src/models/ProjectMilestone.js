const { DataTypes } = require("sequelize");
const sequelize = require("../config/database");
module.exports = sequelize.define("ProjectMilestone", {
  id: { type: DataTypes.BIGINT.UNSIGNED, primaryKey: true, autoIncrement: true },
  project_id: DataTypes.BIGINT.UNSIGNED,
  title: DataTypes.STRING(255),
  description: DataTypes.TEXT,
  status: DataTypes.ENUM("pending","in_progress","completed"),
  due_date: DataTypes.DATEONLY,
  completed_at: DataTypes.DATE,
  created_at: DataTypes.DATE
}, { tableName: "project_milestones", timestamps: false, freezeTableName: true });
