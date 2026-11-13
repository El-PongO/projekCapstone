const { DataTypes } = require("sequelize");
const sequelize = require("../config/database");

const User = sequelize.define("User", {
  id: { type: DataTypes.BIGINT.UNSIGNED, primaryKey: true, autoIncrement: true },
  agent_code: { type: DataTypes.STRING(20), allowNull: false, unique: true },
  name: { type: DataTypes.STRING(255), allowNull: false },
  email: { type: DataTypes.STRING(255), allowNull: false, unique: true },
  password: { type: DataTypes.STRING(255), allowNull: false },
  phone_number: { type: DataTypes.STRING(20), allowNull: false },
  role: { type: DataTypes.ENUM("admin", "upline", "downline"), allowNull: false },
  agent_type: { type: DataTypes.ENUM("internal", "external"), allowNull: false },
  rate_value_percent: { type: DataTypes.DECIMAL(5, 2), allowNull: false },
  fcm_token: { type: DataTypes.TEXT, allowNull: true },
  is_active: { type: DataTypes.BOOLEAN, allowNull: false }
}, {
  tableName: "users",
  underscored: true,
  timestamps: true,
});

module.exports = User;
