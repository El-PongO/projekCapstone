# Jalankan setelah `npm run dev` dan `npm run seed`.
# Gunakan: powershell -ExecutionPolicy Bypass -File .\scripts\smoke.ps1
$ErrorActionPreference = 'Stop'
$base = 'http://127.0.0.1:3000'
Write-Host '1. Health'
Invoke-RestMethod "$base/api/health" | Format-List
Write-Host '2. Login Upline'
$payload = @{ identifier = 'UPL-001'; password = 'password123' } | ConvertTo-Json
$login = Invoke-RestMethod "$base/api/auth/login" -Method POST -ContentType 'application/json' -Body $payload
$headers = @{ Authorization = "Bearer $($login.token)" }
Write-Host "Login berhasil, role: $($login.user.role)"
Write-Host '3. Dashboard'
Invoke-RestMethod "$base/api/dashboard" -Headers $headers | ConvertTo-Json -Depth 8
Write-Host '4. My Performance'
Invoke-RestMethod "$base/api/performance" -Headers $headers | ConvertTo-Json -Depth 8
Write-Host '5. My Downlines'
Invoke-RestMethod "$base/api/upline/downlines" -Headers $headers | ConvertTo-Json -Depth 8
Write-Host '6. My CRM'
Invoke-RestMethod "$base/api/upline/crm" -Headers $headers | ConvertTo-Json -Depth 8
Write-Host 'Semua endpoint GET utama Upline telah dicoba.'
