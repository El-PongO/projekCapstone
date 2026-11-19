const { DataTypes } = require("sequelize");
const sequelize = require("../config/database");
module.exports = sequelize.define("Payment", {
  id: { type: DataTypes.BIGINT.UNSIGNED, primaryKey: true, autoIncrement: true },
  project_id: DataTypes.BIGINT.UNSIGNED,
  invoice_id: DataTypes.BIGINT.UNSIGNED,
  payment_receipt_no: DataTypes.STRING(50),
  amount_paid: DataTypes.DECIMAL(15,2),
  payment_date: DataTypes.DATEONLY,
  payment_method: DataTypes.STRING(50),
  payment_status: DataTypes.ENUM("unpaid","partial","paid"),
  due_date: DataTypes.DATEONLY,
  created_at: DataTypes.DATE
}, { tableName: "payments", timestamps: false, freezeTableName: true });
