const { DataTypes } = require("sequelize");
const sequelize = require("../config/database");
module.exports = sequelize.define("CommissionPayout", {
  id: { type: DataTypes.BIGINT.UNSIGNED, primaryKey: true, autoIncrement: true },
  commission_id: DataTypes.BIGINT.UNSIGNED,
  payout_number: DataTypes.STRING(50),
  amount_paid: DataTypes.DECIMAL(15,2),
  payout_date: DataTypes.DATEONLY,
  payment_method: DataTypes.STRING(50),
  reference_number: DataTypes.STRING(100),
  recorded_by: DataTypes.BIGINT.UNSIGNED,
  notes: DataTypes.TEXT,
  created_at: DataTypes.DATE
}, { tableName: "commission_payouts", timestamps: false, freezeTableName: true });
