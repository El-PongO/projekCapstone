-- Integra Agency - Upgrade dari 15 ke 20 tabel
-- WAJIB: backup database dahulu. Jangan import ulang dump yang mengandung DROP TABLE.
-- Jalankan sekali di SQLyog pada database `integra_agency`.
USE integra_agency;

-- 16. clients
CREATE TABLE IF NOT EXISTS clients (
 id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
 client_code VARCHAR(50) NOT NULL,
 client_name VARCHAR(255) NOT NULL,
 phone_number VARCHAR(20) NULL,
 email VARCHAR(100) NULL,
 address TEXT NULL,
 created_by BIGINT UNSIGNED NOT NULL,
 created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
 updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
 PRIMARY KEY(id),
 UNIQUE KEY uq_clients_code(client_code),
 KEY idx_clients_name(client_name),
 KEY idx_clients_creator(created_by),
 CONSTRAINT fk_clients_creator FOREIGN KEY(created_by) REFERENCES users(id) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 17. meetings
CREATE TABLE IF NOT EXISTS meetings (
 id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
 project_id BIGINT UNSIGNED NOT NULL,
 agent_id BIGINT UNSIGNED NOT NULL,
 meeting_title VARCHAR(255) NOT NULL,
 meeting_at DATETIME NOT NULL,
 meeting_end_at DATETIME NULL,
 location VARCHAR(255) NULL,
 agenda TEXT NULL,
 status ENUM('scheduled','completed','cancelled') NOT NULL DEFAULT 'scheduled',
 notes TEXT NULL,
 created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
 updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
 PRIMARY KEY(id),
 KEY idx_meetings_project(project_id),
 KEY idx_meetings_agent_date(agent_id,meeting_at),
 CONSTRAINT fk_meetings_project FOREIGN KEY(project_id) REFERENCES projects(id) ON DELETE RESTRICT ON UPDATE CASCADE,
 CONSTRAINT fk_meetings_agent FOREIGN KEY(agent_id) REFERENCES users(id) ON DELETE RESTRICT ON UPDATE CASCADE,
 CONSTRAINT chk_meeting_time CHECK(meeting_end_at IS NULL OR meeting_end_at > meeting_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 18. invoices
CREATE TABLE IF NOT EXISTS invoices (
 id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
 invoice_number VARCHAR(50) NOT NULL,
 project_id BIGINT UNSIGNED NOT NULL,
 created_by BIGINT UNSIGNED NOT NULL,
 invoice_type ENUM('dp','termin','final','full') NOT NULL DEFAULT 'full',
 total_amount DECIMAL(15,2) NOT NULL,
 invoice_date DATE NOT NULL,
 due_date DATE NOT NULL,
 status ENUM('unpaid','partial','paid') NOT NULL DEFAULT 'unpaid',
 notes TEXT NULL,
 created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
 updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
 PRIMARY KEY(id),
 UNIQUE KEY uq_invoices_number(invoice_number),
 KEY idx_invoices_project(project_id),
 KEY idx_invoices_creator(created_by),
 KEY idx_invoices_status_due(status,due_date),
 CONSTRAINT fk_invoices_project FOREIGN KEY(project_id) REFERENCES projects(id) ON DELETE RESTRICT ON UPDATE CASCADE,
 CONSTRAINT fk_invoices_creator FOREIGN KEY(created_by) REFERENCES users(id) ON DELETE RESTRICT ON UPDATE CASCADE,
 CONSTRAINT chk_invoice_amount CHECK(total_amount>0),
 CONSTRAINT chk_invoice_due_date CHECK(due_date>=invoice_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 19. commission_payouts
CREATE TABLE IF NOT EXISTS commission_payouts (
 id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
 commission_id BIGINT UNSIGNED NOT NULL,
 payout_number VARCHAR(50) NOT NULL,
 amount_paid DECIMAL(15,2) NOT NULL,
 payout_date DATE NOT NULL,
 payment_method VARCHAR(50) NOT NULL,
 reference_number VARCHAR(100) NULL,
 recorded_by BIGINT UNSIGNED NOT NULL,
 notes TEXT NULL,
 created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
 PRIMARY KEY(id),
 UNIQUE KEY uq_commission_payout_number(payout_number),
 KEY idx_payouts_commission(commission_id),
 KEY idx_payouts_date(payout_date),
 KEY idx_payouts_recorded_by(recorded_by),
 CONSTRAINT fk_payouts_commission FOREIGN KEY(commission_id) REFERENCES commissions(id) ON DELETE RESTRICT ON UPDATE CASCADE,
 CONSTRAINT fk_payouts_recorder FOREIGN KEY(recorded_by) REFERENCES users(id) ON DELETE RESTRICT ON UPDATE CASCADE,
 CONSTRAINT chk_payout_amount CHECK(amount_paid>0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 20. proposal_items
CREATE TABLE IF NOT EXISTS proposal_items (
 id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
 proposal_id BIGINT UNSIGNED NOT NULL,
 item_id BIGINT UNSIGNED NULL,
 item_name VARCHAR(255) NOT NULL,
 description TEXT NULL,
 quantity INT UNSIGNED NOT NULL DEFAULT 1,
 unit_price DECIMAL(15,2) NOT NULL DEFAULT 0,
 created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
 PRIMARY KEY(id),
 KEY idx_proposal_items_proposal(proposal_id),
 KEY idx_proposal_items_item(item_id),
 CONSTRAINT fk_proposal_items_proposal FOREIGN KEY(proposal_id) REFERENCES proposals(id) ON DELETE RESTRICT ON UPDATE CASCADE,
 CONSTRAINT fk_proposal_items_item FOREIGN KEY(item_id) REFERENCES items(id) ON DELETE SET NULL ON UPDATE CASCADE,
 CONSTRAINT chk_proposal_item_quantity CHECK(quantity>0),
 CONSTRAINT chk_proposal_item_price CHECK(unit_price>=0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Kolom untuk menghubungkan tabel lama dengan lima tabel baru.
-- Jika kolom sudah ada karena pernah menjalankan SQL sebelumnya,
-- lewati ALTER terkait (jangan menjalankan dua kali).
ALTER TABLE projects
 ADD COLUMN client_id BIGINT UNSIGNED NULL AFTER agent_id,
 ADD KEY idx_projects_client(client_id),
 ADD CONSTRAINT fk_projects_client FOREIGN KEY(client_id) REFERENCES clients(id) ON DELETE RESTRICT ON UPDATE CASCADE;

ALTER TABLE payments
 ADD COLUMN invoice_id BIGINT UNSIGNED NULL AFTER project_id,
 ADD KEY idx_payments_invoice(invoice_id),
 ADD CONSTRAINT fk_payments_invoice FOREIGN KEY(invoice_id) REFERENCES invoices(id) ON DELETE RESTRICT ON UPDATE CASCADE;

-- Untuk statistik "This Month's Cut" berdasarkan tanggal Deal sesungguhnya.
-- Tidak menambah tabel, hanya satu kolom pada tabel projects.
ALTER TABLE projects ADD COLUMN deal_date DATE NULL AFTER status;

-- Verifikasi jumlah tabel (harus 20):
SELECT COUNT(*) AS total_tabel FROM information_schema.tables
 WHERE table_schema='integra_agency' AND table_type='BASE TABLE';
