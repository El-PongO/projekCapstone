const { DataTypes } = require("sequelize");
const sequelize = require("../config/database");
module.exports = sequelize.define("Meeting", {
  id: { type: DataTypes.BIGINT.UNSIGNED, primaryKey: true, autoIncrement: true },
  project_id: DataTypes.BIGINT.UNSIGNED,
  agent_id: DataTypes.BIGINT.UNSIGNED,
  meeting_title: DataTypes.STRING(255),
  meeting_at: DataTypes.DATE,
  meeting_end_at: DataTypes.DATE,
  location: DataTypes.STRING(255),
  agenda: DataTypes.TEXT,
  status: DataTypes.ENUM("scheduled","completed","cancelled"),
  notes: DataTypes.TEXT,
  created_at: DataTypes.DATE,
  updated_at: DataTypes.DATE
}, { tableName: "meetings", timestamps: false, freezeTableName: true });
