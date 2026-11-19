const { DataTypes } = require("sequelize");
const sequelize = require("../config/database");
module.exports = sequelize.define("User", {
  id: { type: DataTypes.BIGINT.UNSIGNED, primaryKey: true, autoIncrement: true },
  agent_code: { type: DataTypes.STRING(20), allowNull: false },
  name: DataTypes.STRING(255),
  email: DataTypes.STRING(255),
  password: DataTypes.STRING(255),
  phone_number: DataTypes.STRING(20),
  role: DataTypes.ENUM("admin","upline","downline"),
  agent_type: DataTypes.ENUM("internal","external"),
  rate_value_percent: DataTypes.DECIMAL(5,2),
  fcm_token: DataTypes.TEXT,
  is_active: DataTypes.BOOLEAN,
  created_at: DataTypes.DATE,
  updated_at: DataTypes.DATE
}, { tableName: "users", timestamps: false, freezeTableName: true });
