const { Sequelize } = require("sequelize");
module.exports = new Sequelize(
  process.env.DB_NAME || "integra_agency",
  process.env.DB_USER || "root",
  process.env.DB_PASS ?? "",
  {
    host: process.env.DB_HOST || "127.0.0.1",
    port: Number(process.env.DB_PORT || 3306),
    dialect: "mysql",
    logging: process.env.DB_LOG === "true" ? console.log : false,
    timezone: "+07:00",
    pool: { max: 8, min: 0, acquire: 30000, idle: 10000 }
  }
);
