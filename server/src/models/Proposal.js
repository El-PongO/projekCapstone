const { DataTypes } = require("sequelize");
const sequelize = require("../config/database");
module.exports = sequelize.define("Proposal", {
  id: { type: DataTypes.BIGINT.UNSIGNED, primaryKey: true, autoIncrement: true },
  proposal_number: DataTypes.STRING(50),
  project_id: DataTypes.BIGINT.UNSIGNED,
  agent_id: DataTypes.BIGINT.UNSIGNED,
  proposal_title: DataTypes.STRING(255),
  pdf_file_path: DataTypes.STRING(255),
  valid_until: DataTypes.DATEONLY,
  created_at: DataTypes.DATE
}, { tableName: "proposals", timestamps: false, freezeTableName: true });
