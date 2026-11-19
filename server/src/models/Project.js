const { DataTypes } = require("sequelize");
const sequelize = require("../config/database");
module.exports = sequelize.define("Project", {
  id: { type: DataTypes.BIGINT.UNSIGNED, primaryKey: true, autoIncrement: true },
  project_code: DataTypes.STRING(50),
  agent_id: DataTypes.BIGINT.UNSIGNED,
  client_id: DataTypes.BIGINT.UNSIGNED,
  client_name: DataTypes.STRING(255),
  client_phone: DataTypes.STRING(20),
  client_email: DataTypes.STRING(100),
  status: DataTypes.ENUM("follow_up","deal","no_deal","cancelled"),
  execution_stage: DataTypes.ENUM("negotiation","proposal_sent","app_creation","implementation","completed"),
  deal_value: DataTypes.DECIMAL(15,2),
  cancel_reason: DataTypes.TEXT,
  deal_date: DataTypes.DATEONLY,
  created_at: DataTypes.DATE,
  updated_at: DataTypes.DATE
}, { tableName: "projects", timestamps: false, freezeTableName: true });
