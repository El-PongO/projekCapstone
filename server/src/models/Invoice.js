const { DataTypes } = require("sequelize");
const sequelize = require("../config/database");
module.exports = sequelize.define("Invoice", {
  id: { type: DataTypes.BIGINT.UNSIGNED, primaryKey: true, autoIncrement: true },
  invoice_number: DataTypes.STRING(50),
  project_id: DataTypes.BIGINT.UNSIGNED,
  created_by: DataTypes.BIGINT.UNSIGNED,
  invoice_type: DataTypes.ENUM("dp","termin","final","full"),
  total_amount: DataTypes.DECIMAL(15,2),
  invoice_date: DataTypes.DATEONLY,
  due_date: DataTypes.DATEONLY,
  status: DataTypes.ENUM("unpaid","partial","paid"),
  notes: DataTypes.TEXT,
  created_at: DataTypes.DATE,
  updated_at: DataTypes.DATE
}, { tableName: "invoices", timestamps: false, freezeTableName: true });
