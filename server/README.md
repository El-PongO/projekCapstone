# Integra Agency - Backend 20 Tabel

Backend untuk **Admin, Upline, dan Downline**. Teknologi: **Node.js, Express, Sequelize, MySQL**, JWT, bcrypt, PDFKit, ExcelJS, Firebase Admin (opsional). Kode JavaScript biasa, tidak memakai TypeScript. Struktur mengikuti gaya sederhana Tutor M7 (`index.js`, `src/config`, `src/models`, `src/controllers`, `src/routes`, `src/services`, `src/seeders`). Handler untuk modul besar tertentu langsung berada di route agar mudah dilacak.

## Persyaratan

- Node.js **20 atau lebih baru** (direkomendasikan Node 22).
- MySQL 8 (SQL awal pengguna menggunakan MySQL 8.4.3).
- Database utama: `integra_agency`, bukan `integra_agency_db` (ubah `DB_NAME` sesuai database pada SQLyog).
- SQL **15 tabel awal** yang kamu kirim sudah diimpor, dan 5 tabel tambahan belum atau sudah dibuat.

## Langkah instalasi

1. **BACKUP** database yang dipakai sekarang. SQL awal 15 tabel berisi `DROP TABLE`, **jangan di-import ulang** ke database yang berisi data penting.
2. Jika masih ada 15 tabel, buka `db/upgrade_15_to_20.sql` di SQLyog, jalankan **sekali**. Kalau kamu sudah membuat 5 tabel secara manual, **jangan ulang `ALTER TABLE`** yang kolomnya sudah ada. Pastikan `projects.client_id`, `payments.invoice_id`, `projects.deal_date` tersedia.
3. Di folder `server`, jalankan:

```powershell
npm install
Copy-Item .env.example .env
```

4. Edit `.env`: samakan `DB_NAME`, `DB_USER`, `DB_PASS`, dan ganti `JWT_SECRET` string acak minimal 32 karakter. Jika MySQL memakai port bukan 3306, sesuaikan `DB_PORT`.
5. Cek apakah tabel serta kolom cocok:

```powershell
npm run check
```

6. Data demo hanya jika diperlukan (tidak mengubah password akun yang sudah ada):

```powershell
npm run seed
npm run dev
```

7. Test lewat browser: `http://127.0.0.1:3001/api/health`, lalu `http://127.0.0.1:3001/api/health/db`.

## Integrasi Android (Kotlin + Jetpack Compose)

API lama dijaga kompatibel:

- `POST /api/auth/login` menerima `identifier` dan `password`; respons `{ "token", "user": {"id", "agentCode", "name", "email", "role"} }`.
- `GET /api/dashboard` memberikan `{role, stats, recentProjects}`.
- `GET /api/performance` memberikan `{stats:{totalSales,totalIncome,totalDealsMade,monthlyCut}, sales, income}`.
- `GET /api/upline/downlines` dan `GET /api/upline/downlines/:id` cocok dengan `UplineDownlineApi.kt`.
- `GET /api/upline/crm` cocok dengan `CrmResponse` yang diberikan sebelumnya.

Kirim JWT pada header `Authorization: Bearer TOKEN`. Untuk emulator dengan `BuildConfig.API_BASE_URL="http://127.0.0.1:3000/"`, aktifkan ADB reverse setelah restart emulator:

```powershell
& "C:\Android\Sdk\platform-tools\adb.exe" reverse tcp:3000 tcp:3000
```

**Catatan:** Dashboard Upline saat ini pada file Kotlin lama masih mempunyai default data statis. Untuk menggantinya dengan data ini, panggil `GET /api/upline/dashboard`. Halaman Admin dan Downline khusus masih memerlukan kode Kotlin; di sini yang dibuat adalah Backend-nya.

## Urutan bisnis yang wajib

1. Agen/Admin membuat proyek `follow_up` dan item proyek.
2. Agen meminta approval melalui `POST /api/approvals/projects/:projectId/request`.
3. Admin menerima/menolak melalui `POST /api/approvals/:id/review`. Saat **approved**, proyek berubah `deal`, `deal_date` disimpan, komisi agen dan Upline (jika ada) dihitung memakai `rate_value_percent` masing-masing.
4. Admin menyetujui komisi (jika layak): `POST /api/commissions/:id/approve`.
5. Admin membuat invoice, mencatat penerimaan payment, lalu bisa mencatat `commission_payouts`. Total bayar dikunci agar tidak melampaui invoice/komisi.
6. Agen membuat proposal PDF / follow-up / meeting / milestone / dokumen.
7. Admin memantau 10 laporan dan export Excel/PDF.

> **Aturan komisi perlu dikonfirmasi dengan kebijakan bisnis**: contoh ini menggunakan `agent_type=internal` -> 0%; `external` -> `deal_value * rate_value_percent / 100`. Jika ada Upline pada relasi Downline, komisi Upline dihitung tambahan berdasarkan rate Upline yang ditentukan Admin, **bukan potongan dari komisi Downline**. Kebijakan ini tidak otomatis membatasi total gabungan rate beberapa agen; Admin harus mengatur rate sesuai kontrak. Perubahan rate setelah Deal tidak mengubah histori yang tersimpan.

## Notifikasi dan FCM

- Semua notifikasi disimpan pada tabel `notifications` dan dapat dibaca dari `/api/notifications`.
- FCM opsional: isi `FIREBASE_SERVICE_ACCOUNT` dengan **path file JSON Firebase Admin** milik proyek. **Jangan commit file private key** ke Git.
- Android harus mengirim FCM token perangkat ke `POST /api/notifications/device`.
- Jalankan `npm run remind` dengan Windows Task Scheduler (mis. tiap pagi/jam) untuk notifikasi Follow Up dan Meeting yang jatuh tempo dalam 24 jam. Ini **bukan** scheduler yang terus berjalan setelah `npm run dev`.
- Tabel `users.fcm_token` hanya menyimpan **satu perangkat per user**. Untuk multi-device perlu tambahan tabel `user_devices` (akan melebihi 20 tabel) atau strategi token lain.

## Hak akses data

- Admin: seluruh proyek/agen, data master, approval, pembayaran dan laporan.
- Upline: proyek milik sendiri; membaca ringkasan proyek Downline di bawahnya dalam bentuk anonim, tidak melihat nama/telepon/email klien Downline. **Tidak boleh mengubah proyek/dokumen milik Downline**.
- Downline: proyek, jadwal, proposal, komisi, dan dokumen miliknya sendiri. Tidak boleh melihat data klien dari agen lain.
- Hanya Admin yang dapat membuat akun agen, mengubah rate komisi dan membuat invoice/pencairan.

## Validasi dan keterbatasan

- Jalankan `npm test` dan `npm run check` setelah `npm install`.
- `npm run check` melakukan pemeriksaan skema **live** pada database; source ini sudah diperiksa syntax tetapi belum diuji integrasi terhadap MySQL di mesinmu.
- Invoice, payment dan komisi menggunakan Sequelize transaction untuk perhitungan saldo, tetapi pembatalan/pengembalian (refund), pajak, multi-currency, dan pembukuan akuntansi lengkap belum menjadi cakupan.
- Proposal PDF dan dokumen disimpan di `uploads/` lokal, bukan cloud storage. Backup folder ini dan batasi akses server.
- Laporan revenue forecast adalah **heuristik dari pipeline**, bukan model AI atau proyeksi finansial yang teruji.
- Backend adalah basis pengembangan Capstone; sebelum production wajib tambah test integrasi, audit keamanan, rate-limit, backup, serta monitoring.

Lihat `docs/ENDPOINTS.md` untuk contoh request dan daftar API lengkap.

## Smoke test

Di Windows, setelah Backend dan seed aktif: `powershell -ExecutionPolicy Bypass -File .\scripts\smoke.ps1`. Ini menguji read-only GET utama dan Login tanpa mengubah transaksi.
