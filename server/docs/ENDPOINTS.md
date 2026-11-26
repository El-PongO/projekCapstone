# Referensi Endpoint Backend Integra Agency (20 tabel)

**Base URL:** `http://127.0.0.1:3000` (lokal melalui ADB reverse Android Emulator).

Seluruh endpoint selain `/api/auth/login` dan `/api/health*` membutuhkan header:

```text
Authorization: Bearer <TOKEN_JWT>
```

JSON request menggunakan `camelCase`; ORM menerjemahkan ke kolom database `snake_case`.

## Login dan profil - semua role

| Method | URL | Keterangan |
|---|---|---|
| POST | `/api/auth/login` | Login email/kode agen + password |
| GET | `/api/auth/me` | Validasi JWT dan identitas |
| POST | `/api/auth/logout` | Informasi logout; Android harus menghapus token lokal |
| GET | `/api/profile` | Detail akun login |
| PATCH | `/api/profile` | Nama, telepon, FCM token |
| POST | `/api/profile/change-password` | Ganti password |
| GET | `/api/notifications` | Inbox notifikasi |
| PATCH | `/api/notifications/:id/read` | Tandai sudah dibaca |
| PATCH | `/api/notifications/read-all/now` | Baca semua |
| POST | `/api/notifications/device` | Daftarkan FCM token |

### Login request

```json
{"identifier":"UPL-001","password":"password123"}
```

### Login response

```json
{"token":"<JWT>","user":{"id":2,"agentCode":"UPL-001","name":"Budi Upline","email":"upline@integra.local","role":"upline"}}
```

## Dashboard dan Performance

| Role | Method | URL | Fungsi |
|---|---|---|---|
| Semua | GET | `/api/dashboard` | Ringkasan dan recent projects dengan JSON kompatibel Kotlin lama |
| Semua | GET | `/api/performance` | `stats` / `sales` / `income`, + monthlyCut dan totalDealsMade |
| Upline | GET | `/api/upline/dashboard` | Monthly Cut, Projects Held, active deals, aktivitas Downline |
| Upline | GET | `/api/upline/downlines` | Daftar anonim Downline dan performa |
| Upline | GET | `/api/upline/downlines/:id` | Statistik/ringkasan proyek Downline |
| Upline | GET | `/api/upline/crm` | Active, Deals, Dropped dan Summary |
| Downline | GET | `/api/downline/dashboard` | Dashboard Downline, My Upline, tugas mendatang |
| Downline | GET | `/api/agents/my-upline/info` | Kode dan nama Upline |
| Admin | GET | `/api/admin/overview` | Statistik operasional keseluruhan |

## Agen / hierarchy - Admin

| Method | URL | Fungsi |
|---|---|---|
| GET | `/api/agents?role=downline` | Daftar agen (filter role opsional) |
| POST | `/api/agents` | Buat agen upline/downline |
| PATCH | `/api/agents/:id` | Ubah identitas, rate, status aktif, reset password |
| PUT | `/api/agents/:id/upline` | Ubah relasi Downline ke Upline |

```json
{"agentCode":"DNL-002","name":"Rina Downline","email":"rina@example.com","password":"password123","role":"downline","agentType":"external","ratePercent":10,"uplineId":2}
```

## Master kategori dan item

| Method | URL | Role |
|---|---|---|
| GET | `/api/master/categories` | Semua |
| POST | `/api/master/categories` | Admin |
| PATCH | `/api/master/categories/:id` | Admin |
| DELETE | `/api/master/categories/:id` | Admin |
| GET | `/api/master/items` | Semua |
| POST | `/api/master/items` | Admin |
| PATCH | `/api/master/items/:id` | Admin |
| DELETE | `/api/master/items/:id` | Admin |

```json
{"categoryName":"Website","description":"Jasa pengembangan situs"}
```

```json
{"categoryId":1,"itemCode":"ITM-002","itemName":"Landing Page","basePrice":3000000}
```

## Master klien (Admin + Agen)

| Method | URL | Fungsi |
|---|---|---|
| GET | `/api/clients` | Daftar klien yang boleh dibaca |
| GET | `/api/clients/:id` | Detail klien |
| POST | `/api/clients` | Buat klien |
| PATCH | `/api/clients/:id` | Edit klien |
| DELETE | `/api/clients/:id` | Hapus klien yang belum terpakai |

```json
{"clientCode":"CL-002","clientName":"PT Nusantara","phoneNumber":"0812345678","email":"info@example.com","address":"Surabaya"}
```

## Proyek (Admin, Upline, Downline)

| Method | URL | Fungsi |
|---|---|---|
| GET | `/api/projects` | Filter `?status=follow_up` atau `?agentId=2` |
| POST | `/api/projects` | Buat proyek (default follow_up) |
| GET | `/api/projects/:id` | Detail proyek + items + activities, sesuai scope |
| PATCH | `/api/projects/:id` | Ubah data proyek sebelum Deal |
| PUT | `/api/projects/:id/items` | Ganti seluruh detail item sebelum Deal |
| PATCH | `/api/projects/:id/status` | Ubah follow_up, no_deal, cancelled (Deal wajib approval) |
| PATCH | `/api/projects/:id/stage` | Update execution stage |
| POST | `/api/projects/:id/counter-offer` | Counter Offer nilai Deal pada tahap negosiasi |
| DELETE | `/api/projects/:id` | Soft cancel, tidak hapus histori |

```json
{
  "clientId":1,
  "dealValue":15000000,
  "items":[{"itemId":1,"quantity":1,"price":15000000}]
}
```

Jika tidak ada `clientId`, kirim `clientName`, `clientPhone`, `clientEmail` sebagai teks. **Admin** harus menambahkan `agentId` untuk menentukan pemilik proyek. Agen otomatis menjadi pemilik proyek yang dibuatnya.

## Approval → Commission → Payment

| Method | URL | Role | Fungsi |
|---|---|---|---|
| POST | `/api/approvals/projects/:projectId/request` | Agen | Ajukan Deal |
| GET | `/api/approvals/mine` | Agen | Status pengajuan saya |
| GET | `/api/approvals?status=pending` | Admin | Antrian approval |
| POST | `/api/approvals/:id/review` | Admin | Approve atau Reject Deal |
| GET | `/api/commissions` | Admin/Agen | Komisi sesuai role |
| GET | `/api/commissions/:id` | Admin/Agen pemilik | Detail dan riwayat payout |
| POST | `/api/commissions/:id/approve` | Admin | Menyetujui komisi |
| GET | `/api/invoices` | Admin/Agen scope | Invoice proyek |
| POST | `/api/invoices` | Admin | Membuat invoice |
| GET | `/api/invoices/:id` | Admin/Agen scope | Invoice + payments |
| GET | `/api/payments` | Admin/Agen scope | Riwayat penerimaan pembayaran klien |
| POST | `/api/payments` | Admin | Catat pembayaran invoice |
| GET | `/api/commission-payouts` | Admin | Riwayat pencairan komisi |
| POST | `/api/commission-payouts` | Admin | Pencairan komisi, bisa parsial |

### Admin menerima Deal

```json
{"decision":"approved"}
```

atau

```json
{"decision":"rejected","reason":"Dokumen belum lengkap"}
```

### Buat invoice

```json
{"projectId":1,"invoiceType":"dp","totalAmount":5000000,"invoiceDate":"2026-11-12","dueDate":"2026-11-25","notes":"DP"}
```

### Catat pembayaran klien

```json
{"invoiceId":1,"amountPaid":2500000,"paymentDate":"2026-11-12","paymentMethod":"transfer"}
```

### Payout komisi

```json
{"commissionId":1,"amountPaid":1000000,"payoutDate":"2026-11-12","paymentMethod":"transfer","referenceNumber":"TRX-001"}
```

## Follow-up, Schedule, Meetings, Milestones, Documents

| Method | URL | Role |
|---|---|---|
| GET | `/api/productivity/tasks` | Admin lihat semua; agen milik sendiri |
| POST | `/api/productivity/projects/:id/tasks` | Owner proyek/Admin |
| PATCH | `/api/productivity/tasks/:id` | Owner task/Admin |
| GET | `/api/productivity/schedule` | Jadwal gabungan tasks + meetings |
| GET | `/api/productivity/meetings` | Meeting milik sendiri atau filter admin |
| POST | `/api/productivity/projects/:id/meetings` | Owner proyek/Admin |
| PATCH | `/api/productivity/meetings/:id` | Owner meeting/Admin |
| GET | `/api/productivity/projects/:id/milestones` | Role dengan izin baca proyek |
| POST | `/api/productivity/projects/:id/milestones` | Owner proyek/Admin |
| PATCH | `/api/productivity/milestones/:id` | Owner proyek/Admin |
| DELETE | `/api/productivity/milestones/:id` | Owner proyek/Admin |
| GET | `/api/productivity/projects/:id/documents` | Owner proyek/Admin |
| POST | `/api/productivity/projects/:id/documents` | Upload private multipart field `file` dan `documentType` |
| GET | `/api/productivity/documents/:id/download` | Download dokumen private |
| DELETE | `/api/productivity/documents/:id` | Hapus dokumen dan file lokal |

Contoh Follow Up:

```json
{"title":"Kontak ulang klien","dueAt":"2026-11-13T10:00:00+07:00","notes":"Bahas kebutuhan"}
```

Contoh Meeting:

```json
{"meetingTitle":"Presentasi Proposal","meetingAt":"2026-11-14T09:00:00+07:00","meetingEndAt":"2026-11-14T10:00:00+07:00","location":"Google Meet","agenda":"Diskusi penawaran"}
```

Contoh Milestone:

```json
{"title":"Design UI","description":"Review tampilan","dueDate":"2026-11-20"}
```

## Proposal PDF

| Method | URL | Fungsi |
|---|---|---|
| GET | `/api/proposals` | Proposal sendiri/Admin |
| POST | `/api/proposals/projects/:projectId` | Generate proposal PDF dan snapshot item |
| GET | `/api/proposals/:id` | Detail metadata dan proposal_items |
| GET | `/api/proposals/:id/download` | Unduh PDF private |

```json
{
 "title":"Penawaran Sistem Informasi",
 "validUntil":"2026-12-15",
 "items":[{"itemId":1,"itemName":"Pembuatan Website","quantity":1,"unitPrice":15000000,"description":"Responsive website"}]
}
```

## Notifikasi Admin + laporan

| Method | URL | Fungsi |
|---|---|---|
| POST | `/api/notifications/broadcast` | Admin kirim notification center + coba FCM |
| GET | `/api/reports/types` | Daftar 10 jenis laporan |
| GET | `/api/reports/:type?format=json` | Laporan JSON |
| GET | `/api/reports/:type?format=excel` | Unduh XLSX |
| GET | `/api/reports/:type?format=pdf` | Unduh PDF |

Sepuluh nilai `type`:

1. `agent-performance`
2. `commission-sharing`
3. `project-conversion`
4. `deal-summary`
5. `payment-history`
6. `aging-payment`
7. `cancel-analysis`
8. `agent-hierarchy`
9. `project-category`
10. `revenue-forecast` (estimasi sederhana)

Semua endpoint laporan hanya untuk Admin. Parameter opsional `from` dan `to` menggunakan
format `YYYY-MM-DD` dan batas tanggal inklusif. JSON mengembalikan `type`, `title`,
`period`, `count`, `columns` (pasangan `key` dan `label` sesuai urutan tampilan),
`data`, dan `summary` (ringkasan kosong untuk laporan tanpa total). Kolom tetap
tersedia meskipun data kosong. PDF dan Excel menampilkan tabel dengan judul kolom;
ringkasan juga disertakan dalam PDF dan lembar `Ringkasan` pada Excel jika tersedia.

Periode memakai tanggal pembuatan proyek untuk performa, konversi, hierarki proyek,
kategori, dan pipeline; tanggal Deal untuk ringkasan Deal dan bagi hasil; tanggal
pembayaran untuk histori pembayaran; tanggal jatuh tempo invoice untuk piutang; dan
tanggal pembaruan proyek untuk pembatalan. Komisi pada laporan hierarki menggunakan
tanggal pembuatan komisi. Perubahan data proyek setelah pembatalan dapat mengubah
periode pembatalannya.

Laporan bagi hasil menggunakan nilai dan rate komisi yang tersimpan saat Deal, bukan
rate profil agen saat ini. Bagian PT adalah nilai Deal dikurangi seluruh komisi
tercatat, termasuk komisi upline eksternal untuk proyek agen internal. Piutang adalah
sisa **invoice** pada hari ini (WIB), setelah pembayaran sampai hari ini; nilai proyek
yang belum dibuatkan invoice tidak termasuk. Filter tanggal piutang membatasi tanggal
jatuh tempo, bukan tanggal snapshot. Kategori menampilkan jumlah proyek dan jumlah
Deal; `line_value` adalah nilai item pada proyek Deal, bukan alokasi nilai Deal.
Pipeline memakai nilai proyek Follow Up saat ini dan estimasi ilustratif berdasarkan
rasio Deal terhadap seluruh proyek dalam periode yang sama; tidak ada prediksi tanggal
realisasi. Ekspor beberapa laporan di aplikasi menghasilkan satu file per laporan.

## Contoh pengujian cepat PowerShell

```powershell
$body = @{ identifier='UPL-001'; password='password123' } | ConvertTo-Json
$auth = Invoke-RestMethod -Uri 'http://127.0.0.1:3000/api/auth/login' -Method POST -ContentType 'application/json' -Body $body
$headers = @{ Authorization = "Bearer $($auth.token)" }
Invoke-RestMethod -Uri 'http://127.0.0.1:3000/api/upline/crm' -Headers $headers
Invoke-RestMethod -Uri 'http://127.0.0.1:3000/api/upline/downlines' -Headers $headers
Invoke-RestMethod -Uri 'http://127.0.0.1:3000/api/performance' -Headers $headers
```

Jika kosong, pastikan seed demo dan relasi Upline–Downline sudah tersedia. Jika `401`, Login kembali; jika `403`, token berasal dari role yang tidak diizinkan. Jika `500`, lihat terminal Node dan jalankan `npm run check`.
