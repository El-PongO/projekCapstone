-- Integra Agency: tambah kesepakatan komisi PER PROYEK (21st table)
-- WAJIB BACKUP DATABASE SEBELUM MENJALANKAN SQL INI.
-- Jalankan satu kali di MySQL/SQLyog pada database integra_agency.
USE integra_agency;
CREATE TABLE IF NOT EXISTS deal_rate_offers (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
  project_id BIGINT UNSIGNED NOT NULL,
  agent_id BIGINT UNSIGNED NOT NULL,
  proposed_by BIGINT UNSIGNED NOT NULL,
  rate_percent DECIMAL(5,2) NOT NULL,
  status ENUM('pending','accepted','rejected') NOT NULL DEFAULT 'pending',
  proposed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  responded_at DATETIME NULL,
  UNIQUE KEY uq_deal_rate_project_agent(project_id,agent_id),
  KEY idx_deal_rate_agent_status(agent_id,status),
  CONSTRAINT fk_rate_project FOREIGN KEY(project_id) REFERENCES projects(id) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT fk_rate_agent FOREIGN KEY(agent_id) REFERENCES users(id) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT fk_rate_proposed FOREIGN KEY(proposed_by) REFERENCES users(id) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT chk_rate_percent CHECK(rate_percent >= 0 AND rate_percent <= 100)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
SELECT COUNT(*) AS deal_rate_offers_ready FROM deal_rate_offers;
