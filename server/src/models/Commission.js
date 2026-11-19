const { DataTypes } = require("sequelize");
const sequelize = require("../config/database");
module.exports = sequelize.define("Commission", {
  id: { type: DataTypes.BIGINT.UNSIGNED, primaryKey: true, autoIncrement: true },
  project_id: DataTypes.BIGINT.UNSIGNED,
  agent_id: DataTypes.BIGINT.UNSIGNED,
  agent_type: DataTypes.ENUM("internal","external"),
  commission_rate_percent: DataTypes.DECIMAL(5,2),
  total_commission_amount: DataTypes.DECIMAL(15,2),
  payout_status: DataTypes.ENUM("pending","approved","paid"),
  payout_date: DataTypes.DATE,
  notes: DataTypes.TEXT,
  created_at: DataTypes.DATE
}, { tableName: "commissions", timestamps: false, freezeTableName: true });
