require("dotenv").config();
const express = require("express");
const cors = require("cors");
const sequelize = require("./src/config/database");
const authRoute = require("./src/routes/authRoute");
const dashboardRoute = require("./src/routes/dashboardRoute");
const performanceRoute = require("./src/routes/performanceRoute");

const uplineDownlineRoute = require("./src/routes/uplineDownlineRoute");


const app = express();
const PORT = process.env.PORT || 3000;

app.use(cors());
app.use(express.json());
app.use(express.urlencoded({ extended: true }));

// Log permintaan API hanya saat development. Tidak mencatat password/token/body.
if (process.env.NODE_ENV !== "production") {
  app.use((req, res, next) => {
    res.on("finish", () => {
      console.log(`[API] ${req.method} ${req.path} -> HTTP ${res.statusCode}`);
    });
    next();
  });
}

app.get("/api/health", (req, res) => res.json({ ok: true }));
app.get("/api/health/db", async (req, res) => {
  try {
    await sequelize.authenticate();
    res.json({ ok: true, database: "connected" });
  } catch (err) {
    res.status(503).json({ ok: false, database: "unavailable" });
  }
});

app.use("/api/auth", authRoute);
app.use("/api/dashboard", dashboardRoute);
app.use("/api/performance", performanceRoute);
app.use("/api/upline/downlines", uplineDownlineRoute);


if (!process.env.JWT_SECRET || process.env.JWT_SECRET.length < 32) {
  throw new Error("JWT_SECRET minimal 32 karakter wajib diisi dalam .env");
}

// Database dari file SQL; tidak menjalankan sync({ force: true }) agar data tidak terhapus.
sequelize.authenticate()
  .then(() => {
    app.listen(PORT, "0.0.0.0", () => {
      console.log(`Server jalan di http://localhost:${PORT}`);
    });
  })
  .catch((err) => {
    console.error("Gagal konek database:", err.message);
    process.exit(1);
  });
