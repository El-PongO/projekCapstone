const { DataTypes } = require("sequelize");
const sequelize = require("../config/database");
module.exports = sequelize.define("Category", {
  id: { type: DataTypes.BIGINT.UNSIGNED, primaryKey: true, autoIncrement: true },
  category_name: DataTypes.STRING(100),
  description: DataTypes.TEXT
}, { tableName: "categories", timestamps: false, freezeTableName: true });
