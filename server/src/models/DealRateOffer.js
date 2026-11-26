const { DataTypes } = require('sequelize');
const sequelize = require('../config/database');
module.exports = sequelize.define('DealRateOffer', {
  id: { type: DataTypes.BIGINT.UNSIGNED, primaryKey: true, autoIncrement: true },
  project_id: { type: DataTypes.BIGINT.UNSIGNED, allowNull: false },
  agent_id: { type: DataTypes.BIGINT.UNSIGNED, allowNull: false },
  proposed_by: { type: DataTypes.BIGINT.UNSIGNED, allowNull: false },
  rate_percent: { type: DataTypes.DECIMAL(5,2), allowNull: false },
  status: { type: DataTypes.ENUM('pending','accepted','rejected'), allowNull: false, defaultValue: 'pending' },
  proposed_at: { type: DataTypes.DATE, allowNull: false },
  responded_at: DataTypes.DATE
}, { tableName:'deal_rate_offers', timestamps:false, freezeTableName:true });
