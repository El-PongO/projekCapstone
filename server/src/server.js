require('dotenv').config();
const express = require('express');
const cors = require('cors');

const app = express();
app.use(cors());
app.use(express.json());

app.get('/api/health', (_req, res) => res.json({ ok: true }));
app.use('/api/auth', require('./routes/auth'));
app.use('/api/performance', require('./routes/performance'));

const port = process.env.PORT || 3000;
app.listen(port, '0.0.0.0', () => console.log(`API listening on :${port}`));
