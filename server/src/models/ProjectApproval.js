const { DataTypes } = require("sequelize");
const sequelize = require("../config/database");
module.exports = sequelize.define("ProjectApproval", {
  id: { type: DataTypes.BIGINT.UNSIGNED, primaryKey: true, autoIncrement: true },
  project_id: DataTypes.BIGINT.UNSIGNED,
  requested_by: DataTypes.BIGINT.UNSIGNED,
  reviewed_by: DataTypes.BIGINT.UNSIGNED,
  status: DataTypes.ENUM("pending","approved","rejected"),
  reason: DataTypes.TEXT,
  created_at: DataTypes.DATE,
  reviewed_at: DataTypes.DATE
}, { tableName: "project_approvals", timestamps: false, freezeTableName: true });
