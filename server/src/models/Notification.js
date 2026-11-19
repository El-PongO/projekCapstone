const { DataTypes } = require("sequelize");
const sequelize = require("../config/database");
module.exports = sequelize.define("Notification", {
  id: { type: DataTypes.BIGINT.UNSIGNED, primaryKey: true, autoIncrement: true },
  user_id: DataTypes.BIGINT.UNSIGNED,
  title: DataTypes.STRING(255),
  message: DataTypes.TEXT,
  type: DataTypes.ENUM("project_status","commission","broadcast","system"),
  is_read: DataTypes.BOOLEAN,
  created_at: DataTypes.DATE
}, { tableName: "notifications", timestamps: false, freezeTableName: true });
