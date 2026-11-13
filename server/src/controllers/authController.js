const { Op } = require("sequelize");
const bcrypt = require("bcryptjs");
const jwt = require("jsonwebtoken");
const User = require("../models/User");

const login = async (req, res) => {
  const { identifier, password } = req.body || {};

  if (!identifier || !password || typeof identifier !== "string" || typeof password !== "string") {
    return res.status(400).json({ error: "Email/kode agen dan password wajib diisi" });
  }

  try {
    const input = identifier.trim();
    const user = await User.findOne({
      where: { [Op.or]: [{ email: input }, { agent_code: input }] }
    });

    // Pesan sama agar tidak membocorkan keberadaan akun.
    if (!user || !user.is_active || !(await bcrypt.compare(password, user.password))) {
      return res.status(401).json({ error: "Email/kode agen atau password salah" });
    }

    const token = jwt.sign(
      { sub: Number(user.id), role: user.role },
      process.env.JWT_SECRET,
      { expiresIn: "7d" }
    );

    res.json({
      token: token,
      user: {
        id: Number(user.id),
        agentCode: user.agent_code,
        name: user.name,
        email: user.email,
        role: user.role
      }
    });
  } catch (err) {
    console.error(err);
    res.status(500).json({ error: "Server error" });
  }
};

module.exports = { login };
