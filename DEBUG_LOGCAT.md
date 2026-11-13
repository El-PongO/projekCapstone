# Debug koneksi Login Integra Agency

Project ini punya log khusus Android `IntegraAPI` dan `IntegraLogin` dan log server `[API]`.

1. Jalankan Node.js di folder `server`: `npm run dev`. Pastikan port sama dengan `server/.env` (`PORT=3000`).
2. Jalankan sekali `& "C:\Android\Sdk\platform-tools\adb.exe" reverse tcp:3000 tcp:3000` saat emulator menyala.
3. Tambahkan `apiBaseUrl=http://127.0.0.1:3000/` ke **local.properties** Android; pertahankan `sdk.dir`. Jangan tulis `3001` jika server berada pada `3000`.
4. Gradle Sync, Rebuild, lalu Run ulang aplikasi. Buka Android Studio > Logcat dan filter `tag:IntegraAPI | tag:IntegraLogin` (atau gunakan terminal seperti di bawah).
5. Klik Masuk. Logcat akan menampilkan `ACTIVE BASE URL`, `REQUEST`, `RESPONSE`, atau `NETWORK ERROR` / `LOGIN_FAILED`.

Alternatif PowerShell untuk melihat log debug:

```powershell
& "C:\Android\Sdk\platform-tools\adb.exe" logcat -s IntegraAPI:D IntegraLogin:D '*:S'
```

Tidak ada password atau token yang dicetak ke Logcat. Jangan ubah interceptor untuk mencetak body Login.

Interpretasi:
- `REQUEST ... :3001` -> alamat masih salah; sesuaikan `local.properties`, Sync/Rebuild, uninstall app jika perlu.
- `NETWORK ERROR ConnectException` -> port/backend belum bisa dijangkau; uji `/api/health` di Chrome emulator.
- `RESPONSE HTTP 401` -> koneksi berhasil, kredensial/login yang perlu dicek.
- `RESPONSE HTTP 500` -> backend/database bermasalah, cek terminal VS Code.
- `RESPONSE HTTP 200` lalu `LOGIN_FAILED JsonDataException` -> response server tidak sesuai dengan data class Android.
- Tidak ada `[API] POST /api/auth/login` di terminal -> request belum mencapai Node.js.

Jangan anggap `/api/health` sukses sebagai bukti MySQL sukses; uji `/api/health/db` secara terpisah.
