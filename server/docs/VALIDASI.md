# Status validasi paket

- Jumlah tabel asli pada file pengguna `tabel revisi 15.sql`: **15**.
- Jumlah tabel baru pada `db/upgrade_15_to_20.sql`: **5**.
- Jumlah Sequelize model: **20**.
- Semua kolom model sudah dicocokkan secara statis dengan deklarasi SQL 15 tabel awal + SQL upgrade, termasuk `client_id`, `invoice_id`, dan `deal_date`.
- Seluruh source `.js` lolos `node --check` (pemeriksaan sintaks JavaScript).
- Seluruh `require('./...')` atau `require('../...')` lokal menunjuk ke file yang tersedia.
- **Belum diuji secara runtime** terhadap database MySQL pribadi pengguna: tidak tersedia koneksi ke instance MySQL pengguna.
- **Dependency tidak dapat diinstal di lingkungan pengemasan offline**. Jalankan `npm install` di komputer dengan akses npm, lalu `npm run check` dan `npm test`.
- Lakukan backup data sebelum menjalankan SQL migration. Jangan menjalankan ulang SQL 15 tabel awal yang memiliki `DROP TABLE`.
- Export PDF/Excel dan Firebase FCM memerlukan `npm install`, serta FCM memerlukan file credentials Firebase yang disediakan pengguna.
