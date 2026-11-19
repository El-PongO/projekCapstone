const { DataTypes } = require("sequelize");
const sequelize = require("../config/database");
module.exports = sequelize.define("Item", {
  id: { type: DataTypes.BIGINT.UNSIGNED, primaryKey: true, autoIncrement: true },
  category_id: DataTypes.BIGINT.UNSIGNED,
  item_code: DataTypes.STRING(50),
  item_name: DataTypes.STRING(255),
  base_price: DataTypes.DECIMAL(15,2),
  description: DataTypes.TEXT
}, { tableName: "items", timestamps: false, freezeTableName: true });
