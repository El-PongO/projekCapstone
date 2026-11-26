# PT Integra Ciptadata – Agency Management App

Android app (Kotlin) + Node.js API + MySQL.
Roles: **Upline**, **Downline**, **Admin**. Currently implemented: login and the Upline home page (other roles show a placeholder screen).

```
app/      Android app (Kotlin, XML views)
server/   Node.js REST API (Express + MySQL)
  db/     integra_agency_db.sql  – schema + seed data
```

The app never talks to MySQL directly. It calls the API, which queries MySQL.

## Requirements
- Android Studio (JDK 11+), Android SDK 37
- Node.js 20+
- MySQL 8 running on port 3306

## 1. Database
Import the schema and seed data (creates `integra_agency_db`):

```
mysql -u root -p < server/db/integra_agency_db.sql
```

(Or open the file in SQLyog / MySQL Workbench and run it.)

## 2. API server
```
cd server
npm install
copy .env.example .env      # then edit DB_USER / DB_PASSWORD / JWT_SECRET
npm run seed-passwords      # sets every seeded user's password to "password123"
npm start                   # http://localhost:3000
```

The seed SQL contains placeholder passwords that are not valid bcrypt hashes, so `npm run seed-passwords` is required once after importing.
Use another password with `npm run seed-passwords -- <password>`.

Check it: `curl http://localhost:3000/api/health` → `{"ok":true}`

## 3. Android app
1. Open the project root in Android Studio and let Gradle sync.
2. Make sure the API is running.
3. Run on an **emulator** or a **USB-connected phone**. Building runs `adb reverse tcp:3000 tcp:3000` automatically, so the app can reach the API at `http://127.0.0.1:3000/`. **Start the API (step 2) first.** If the device connects after the build, run it manually (`adb` is in `%LOCALAPPDATA%\Android\Sdk\platform-tools`).
4. For a phone on **Wi-Fi** instead, add to `local.properties`:
   ```
   apiBaseUrl=http://<your-pc-ip>:3000/
   ```
   and allow port 3000 through your firewall.

If login says "Can't reach the server", the API isn't running or the port forward is missing.

Debug builds allow plain `http`; release builds should use HTTPS.

## Demo logins
Log in with an agent code or email. Password: `password123` (after seeding).

| Agent code | Email | Role |
|---|---|---|
| ADM-001 | admin@integraciptadata.com | Admin |
| UPL-001 | budi.upline@gmail.com | Upline (opens the Upline home) |
| DNL-001 | ahmad.downline@gmail.com | Downline |
| DNL-002 | siti.internal@gmail.com | Downline (internal) |

## Notes
- Login and **My Performance** (`GET /api/performance`, JWT-protected) use the API. The rest of the Upline home (stats, deals, activity) is still placeholder data in `UplineHomeActivity`.
- Commission is only calculated by the DB trigger when a project is *updated* to status `deal`, so seeded rows show 0 commission until updated.
- Add new endpoints in `server/src/routes/` and register them in `server/src/server.js`.
- `.env` and `node_modules` are git-ignored; never commit real credentials.
