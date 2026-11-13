const { DataTypes } = require("sequelize");
const sequelize = require("../config/database");

const Project = sequelize.define("Project", {
  id: { type: DataTypes.BIGINT.UNSIGNED, primaryKey: true, autoIncrement: true },
  project_code: { type: DataTypes.STRING(50), allowNull: false },
  agent_id: { type: DataTypes.BIGINT.UNSIGNED, allowNull: false },
  client_name: { type: DataTypes.STRING(255), allowNull: false },
  status: { type: DataTypes.STRING(20), allowNull: false },
  execution_stage: { type: DataTypes.STRING(30), allowNull: false },
  deal_value: { type: DataTypes.DECIMAL(15, 2), allowNull: false },
  calculated_commission: { type: DataTypes.DECIMAL(15, 2), allowNull: false }
}, {
  tableName: "projects",
  underscored: true,
  timestamps: true,
});

module.exports = Project;
