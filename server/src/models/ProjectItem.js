const { DataTypes } = require("sequelize");
const sequelize = require("../config/database");
module.exports = sequelize.define("ProjectItem", {
  id: { type: DataTypes.BIGINT.UNSIGNED, primaryKey: true, autoIncrement: true },
  project_id: DataTypes.BIGINT.UNSIGNED,
  item_id: DataTypes.BIGINT.UNSIGNED,
  quantity: DataTypes.INTEGER.UNSIGNED,
  price: DataTypes.DECIMAL(15,2)
}, { tableName: "project_items", timestamps: false, freezeTableName: true });
