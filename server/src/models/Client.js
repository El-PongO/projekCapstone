const { DataTypes } = require("sequelize");
const sequelize = require("../config/database");
module.exports = sequelize.define("Client", {
  id: { type: DataTypes.BIGINT.UNSIGNED, primaryKey: true, autoIncrement: true },
  client_code: DataTypes.STRING(50),
  client_name: DataTypes.STRING(255),
  phone_number: DataTypes.STRING(20),
  email: DataTypes.STRING(100),
  address: DataTypes.TEXT,
  created_by: DataTypes.BIGINT.UNSIGNED,
  created_at: DataTypes.DATE,
  updated_at: DataTypes.DATE
}, { tableName: "clients", timestamps: false, freezeTableName: true });
