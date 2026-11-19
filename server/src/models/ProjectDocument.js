const { DataTypes } = require("sequelize");
const sequelize = require("../config/database");
module.exports = sequelize.define("ProjectDocument", {
  id: { type: DataTypes.BIGINT.UNSIGNED, primaryKey: true, autoIncrement: true },
  project_id: DataTypes.BIGINT.UNSIGNED,
  uploaded_by: DataTypes.BIGINT.UNSIGNED,
  document_type: DataTypes.STRING(50),
  original_filename: DataTypes.STRING(255),
  file_path: DataTypes.STRING(500),
  created_at: DataTypes.DATE
}, { tableName: "project_documents", timestamps: false, freezeTableName: true });
