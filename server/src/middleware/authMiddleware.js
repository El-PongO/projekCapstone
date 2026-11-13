const jwt = require("jsonwebtoken");
const User = require("../models/User");

const requireAuth = async (req, res, next) => {
  const authorization = req.headers.authorization || "";
  const token = authorization.startsWith("Bearer ")
    ? authorization.substring(7) : null;

  if (!token) return res.status(401).json({ error: "Token tidak ditemukan" });

  try {
    const decoded = jwt.verify(token, process.env.JWT_SECRET);
    const user = await User.findByPk(decoded.sub);
    if (!user || !user.is_active) {
      return res.status(401).json({ error: "Akun tidak aktif atau tidak ditemukan" });
    }
    req.user = { id: Number(user.id), role: user.role };
    next();
  } catch (err) {
    if (err.name === "JsonWebTokenError" || err.name === "TokenExpiredError") {
      return res.status(401).json({ error: "Token tidak valid atau kedaluwarsa" });
    }
    console.error(err);
    return res.status(500).json({ error: "Server error" });
  }
};

module.exports = requireAuth;
