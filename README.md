# Integra Agency — Gaya Tutor M7

Proyek ini menyesuaikan **gaya coding** dari `tutorM7nmp.zip`, dengan tampilan Jetpack Compose Integra Agency **tetap dipertahankan**.

## Perbedaan dari tutor

- Tutor: Kotlin XML + Fragment + ViewBinding; project ini: **Jetpack Compose** sesuai permintaan Capstone sebelumnya.
- Komunikasi API sama seperti tutor: `App.kt` (singleton Retrofit + Moshi), `WebService.kt` (endpoint), `Models.kt` (data class), `ApiService.kt` (pemanggilan API singkat).
- Backend mengikuti tutor: `index.js`, `config/database.js`, `models`, `controllers`, `routes`, `seeders` dengan Sequelize + MySQL.
- Endpoint dan bentuk JSON disesuaikan dengan aplikasi yang sudah ada, jadi dashboard tidak perlu dirombak.

## Menjalankan Backend Node.js

1. Buka folder `server`, lalu jalankan `npm i`.
2. Siapkan MySQL dan import `server/db/integra_agency_db.sql` ke **database kosong khusus demo**. SQL dapat menghapus tabel/data yang sudah ada.
3. Copy `.env.example` ke `.env`. Isi `DB_NAME`, `DB_USER`, `DB_PASS` (atau `DB_PASSWORD`), `JWT_SECRET` acak panjang (minimal 32 karakter). Gunakan database demo, bukan database produksi.
4. Jalankan `npm run seed` untuk mengubah **hanya** password akun `ADM-001`, `UPL-001`, `DNL-001` menjadi `password123`.
5. Jalankan `npm run dev`, dan periksa `http://localhost:3000/api/health` dan `http://localhost:3000/api/health/db`.

Catatan: alih-alih `sequelize.sync({ force:true })` seperti tutor, server menggunakan database yang diimpor melalui SQL karena banyak tabel terkait dan tidak boleh terhapus saat server start.

## Android Emulator

1. Buka root project di Android Studio, lakukan Gradle Sync.
2. Untuk Emulator standar, tambahkan ke `local.properties`:

   ```properties
   apiBaseUrl=http://10.0.2.2:3000/
   ```

3. Jika koneksi 10.0.2.2 gagal, jalankan di PowerShell Windows:

   ```powershell
   & "C:\Android\Sdk\platform-tools\adb.exe" devices
   & "C:\Android\Sdk\platform-tools\adb.exe" reverse tcp:3000 tcp:3000
   ```

   Lalu ubah `local.properties`:

   ```properties
   apiBaseUrl=http://127.0.0.1:3000/
   ```

   Uji `http://127.0.0.1:3000/api/health` dari Chrome Emulator, lalu **Sync + Rebuild + Run** project Android.

4. Login demo: `ADM-001` / `password123`. Untuk Upline gunakan `UPL-001`, untuk Downline `DNL-001` (password sama).

## Struktur utama

```text
app/src/main/java/com/example/projectcapstone/
  App.kt              # Retrofit singleton dan Moshi
  WebService.kt       # interface GET / POST
  Models.kt           # data class
  ApiService.kt       # login, dashboard, performance
  MainActivity.kt     # tampilan Jetpack Compose asli

server/
  index.js
  src/config/database.js
  src/models/User.js
  src/models/Project.js
  src/models/AgentHierarchy.js
  src/controllers/authController.js
  src/controllers/dashboardController.js
  src/controllers/performanceController.js
  src/routes/authRoute.js
  src/routes/dashboardRoute.js
  src/routes/performanceRoute.js
  src/middleware/authMiddleware.js
  src/seeders/seed.js
```

## Status pengujian

File JavaScript divalidasi dengan `node --check` saat paket dibuat. Compilation Android dan autentikasi terhadap database lokal **belum diuji** di mesin pengguna.
