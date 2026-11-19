const { DataTypes } = require("sequelize");
const sequelize = require("../config/database");
module.exports = sequelize.define("ProposalItem", {
  id: { type: DataTypes.BIGINT.UNSIGNED, primaryKey: true, autoIncrement: true },
  proposal_id: DataTypes.BIGINT.UNSIGNED,
  item_id: DataTypes.BIGINT.UNSIGNED,
  item_name: DataTypes.STRING(255),
  description: DataTypes.TEXT,
  quantity: DataTypes.INTEGER.UNSIGNED,
  unit_price: DataTypes.DECIMAL(15,2),
  created_at: DataTypes.DATE
}, { tableName: "proposal_items", timestamps: false, freezeTableName: true });
