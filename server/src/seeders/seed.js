require("dotenv").config();
const bcrypt = require("bcryptjs");
const { Op } = require("sequelize");
const sequelize = require("../config/database");
const User = require("../models/User");

const seed = async () => {
  if (process.env.NODE_ENV === "production") {
    throw new Error("Seeder demo tidak boleh dijalankan di production");
  }

  await sequelize.authenticate();
  const password = process.argv[2] || "password123";
  const hash = await bcrypt.hash(password, 10);
  const demoCodes = ["ADM-001", "UPL-001", "DNL-001"];

  // Hanya ubah akun demo agar password akun asli tidak ikut terganti.
  const [updated] = await User.update(
    { password: hash },
    { where: { agent_code: { [Op.in]: demoCodes } } }
  );

  console.log(`${updated} akun demo berhasil diperbarui`);
  if (updated === 0) {
    console.log("Akun demo belum ada. Import server/db/integra_agency_db.sql terlebih dahulu.");
  }
  await sequelize.close();
};

seed().catch((err) => {
  console.error("Seeder gagal:", err.message);
  process.exit(1);
});
