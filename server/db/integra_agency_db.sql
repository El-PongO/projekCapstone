/*
SQLyog Community v13.3.0 (64 bit)
MySQL - 8.0.30 : Database - integra_agency_db
*********************************************************************
*/

/*!40101 SET NAMES utf8 */;

/*!40101 SET SQL_MODE=''*/;

/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;
CREATE DATABASE /*!32312 IF NOT EXISTS*/`integra_agency_db` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci */ /*!80016 DEFAULT ENCRYPTION='N' */;

USE `integra_agency_db`;

/*Table structure for table `agent_hierarchies` */

DROP TABLE IF EXISTS `agent_hierarchies`;

CREATE TABLE `agent_hierarchies` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `upline_id` bigint unsigned NOT NULL,
  `downline_id` bigint unsigned NOT NULL,
  `depth` int NOT NULL DEFAULT '1',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `downline_id` (`downline_id`),
  KEY `fk_hierarchy_upline` (`upline_id`),
  CONSTRAINT `fk_hierarchy_downline` FOREIGN KEY (`downline_id`) REFERENCES `users` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_hierarchy_upline` FOREIGN KEY (`upline_id`) REFERENCES `users` (`id`) ON DELETE CASCADE,
  CONSTRAINT `chk_no_self_reference` CHECK ((`upline_id` <> `downline_id`))
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

/*Data for the table `agent_hierarchies` */

insert  into `agent_hierarchies`(`id`,`upline_id`,`downline_id`,`depth`,`created_at`) values 
(1,2,3,1,'2026-10-30 00:25:49'),
(2,2,4,1,'2026-10-30 00:25:49');

/*Table structure for table `categories` */

DROP TABLE IF EXISTS `categories`;

CREATE TABLE `categories` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `category_name` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `description` text COLLATE utf8mb4_unicode_ci,
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `category_name` (`category_name`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

/*Data for the table `categories` */

insert  into `categories`(`id`,`category_name`,`description`,`created_at`,`updated_at`) values 
(1,'Enterprise Resource Planning (ERP)','Sistem manajemen terintegrasi proses bisnis perusahaan','2026-10-30 00:25:49','2026-10-30 00:25:49'),
(2,'Learning Management System (LMS)','Platform pembelajaran digital dan manajemen kursus','2026-10-30 00:25:49','2026-10-30 00:25:49'),
(3,'Sistem Klinik & Kesehatan','Aplikasi manajemen rekam medis dan operasional klinik','2026-10-30 00:25:49','2026-10-30 00:25:49');

/*Table structure for table `items` */

DROP TABLE IF EXISTS `items`;

CREATE TABLE `items` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `category_id` bigint unsigned NOT NULL,
  `item_code` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  `item_name` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `base_price` decimal(15,2) NOT NULL DEFAULT '0.00',
  `description` text COLLATE utf8mb4_unicode_ci,
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `item_code` (`item_code`),
  KEY `fk_items_category` (`category_id`),
  CONSTRAINT `fk_items_category` FOREIGN KEY (`category_id`) REFERENCES `categories` (`id`) ON DELETE RESTRICT
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

/*Data for the table `items` */

insert  into `items`(`id`,`category_id`,`item_code`,`item_name`,`base_price`,`description`,`created_at`,`updated_at`) values 
(1,1,'ITM-ERP-01','Core ERP Module System',75000000.00,'Modul utama keuangan, inventaris, dan SDM','2026-10-30 00:25:49','2026-10-30 00:25:49'),
(2,2,'ITM-LMS-01','Standard LMS Platform',35000000.00,'Sistem manajemen ujian dan kelas daring','2026-10-30 00:25:49','2026-10-30 00:25:49'),
(3,3,'ITM-CLN-01','Clinic Management System',25000000.00,'Sistem pendaftaran, kasir, dan rekam medis pasien','2026-10-30 00:25:49','2026-10-30 00:25:49');

/*Table structure for table `payments` */

DROP TABLE IF EXISTS `payments`;

CREATE TABLE `payments` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `project_id` bigint unsigned NOT NULL,
  `payment_receipt_no` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  `amount_paid` decimal(15,2) NOT NULL DEFAULT '0.00',
  `payment_date` date NOT NULL,
  `payment_method` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  `payment_status` enum('unpaid','partial','paid') COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'unpaid',
  `due_date` date NOT NULL,
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `payment_receipt_no` (`payment_receipt_no`),
  KEY `fk_payments_project` (`project_id`),
  KEY `idx_payment_status` (`payment_status`),
  KEY `idx_due_date` (`due_date`),
  CONSTRAINT `fk_payments_project` FOREIGN KEY (`project_id`) REFERENCES `projects` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

/*Data for the table `payments` */

insert  into `payments`(`id`,`project_id`,`payment_receipt_no`,`amount_paid`,`payment_date`,`payment_method`,`payment_status`,`due_date`,`created_at`) values 
(1,1,'PAY-2026-001',10000000.00,'2026-09-10','BCA Virtual Account','partial','2026-10-10','2026-10-30 00:25:49');

/*Table structure for table `project_activity_logs` */

DROP TABLE IF EXISTS `project_activity_logs`;

CREATE TABLE `project_activity_logs` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `project_id` bigint unsigned NOT NULL,
  `user_id` bigint unsigned NOT NULL,
  `activity_description` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `previous_stage` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `current_stage` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `fk_activity_project` (`project_id`),
  KEY `fk_activity_user` (`user_id`),
  CONSTRAINT `fk_activity_project` FOREIGN KEY (`project_id`) REFERENCES `projects` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_activity_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE RESTRICT
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

/*Data for the table `project_activity_logs` */

insert  into `project_activity_logs`(`id`,`project_id`,`user_id`,`activity_description`,`previous_stage`,`current_stage`,`created_at`) values 
(1,1,3,'Menyelesaikan pembayaran DP sebesar Rp 10.000.000','proposal_sent','implementation','2026-10-30 00:25:49');

/*Table structure for table `project_items` */

DROP TABLE IF EXISTS `project_items`;

CREATE TABLE `project_items` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `project_id` bigint unsigned NOT NULL,
  `item_id` bigint unsigned NOT NULL,
  `quantity` int NOT NULL DEFAULT '1',
  `price` decimal(15,2) NOT NULL DEFAULT '0.00',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `fk_project_items_project` (`project_id`),
  KEY `fk_project_items_item` (`item_id`),
  CONSTRAINT `fk_project_items_item` FOREIGN KEY (`item_id`) REFERENCES `items` (`id`) ON DELETE RESTRICT,
  CONSTRAINT `fk_project_items_project` FOREIGN KEY (`project_id`) REFERENCES `projects` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

/*Data for the table `project_items` */

insert  into `project_items`(`id`,`project_id`,`item_id`,`quantity`,`price`,`created_at`) values 
(1,1,3,1,25000000.00,'2026-10-30 00:25:49'),
(2,2,1,1,75000000.00,'2026-10-30 00:25:49');

/*Table structure for table `projects` */

DROP TABLE IF EXISTS `projects`;

CREATE TABLE `projects` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `project_code` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  `agent_id` bigint unsigned NOT NULL,
  `client_name` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `client_phone` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL,
  `client_email` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `status` enum('follow_up','deal','no_deal','cancelled') COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'follow_up',
  `execution_stage` enum('negotiation','proposal_sent','app_creation','implementation','completed') COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'negotiation',
  `deal_value` decimal(15,2) NOT NULL DEFAULT '0.00',
  `calculated_commission` decimal(15,2) NOT NULL DEFAULT '0.00',
  `cancel_reason` text COLLATE utf8mb4_unicode_ci,
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `project_code` (`project_code`),
  KEY `fk_projects_agent` (`agent_id`),
  KEY `idx_project_status` (`status`),
  KEY `idx_execution_stage` (`execution_stage`),
  CONSTRAINT `fk_projects_agent` FOREIGN KEY (`agent_id`) REFERENCES `users` (`id`) ON DELETE RESTRICT
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

/*Data for the table `projects` */

insert  into `projects`(`id`,`project_code`,`agent_id`,`client_name`,`client_phone`,`client_email`,`status`,`execution_stage`,`deal_value`,`calculated_commission`,`cancel_reason`,`created_at`,`updated_at`) values 
(1,'PRJ-2026-001',3,'Klinik Sehat Bersama','081999888777','info@sehatbersama.com','deal','implementation',25000000.00,0.00,NULL,'2026-10-30 00:25:49','2026-10-30 00:25:49'),
(2,'PRJ-2026-002',2,'PT Trans Nusantara','081777666555','procurement@transnusantara.com','follow_up','negotiation',75000000.00,0.00,NULL,'2026-10-30 00:25:49','2026-10-30 00:25:49');

/*Table structure for table `users` */

DROP TABLE IF EXISTS `users`;

CREATE TABLE `users` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `agent_code` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL,
  `name` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `email` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `password` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `phone_number` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL,
  `role` enum('admin','upline','downline') COLLATE utf8mb4_unicode_ci NOT NULL,
  `agent_type` enum('internal','external') COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'external',
  `rate_value_percent` decimal(5,2) NOT NULL DEFAULT '0.00',
  `fcm_token` text COLLATE utf8mb4_unicode_ci,
  `is_active` tinyint(1) NOT NULL DEFAULT '1',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `agent_code` (`agent_code`),
  UNIQUE KEY `email` (`email`),
  KEY `idx_role` (`role`),
  KEY `idx_agent_code` (`agent_code`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

/*Data for the table `users` */

insert  into `users`(`id`,`agent_code`,`name`,`email`,`password`,`phone_number`,`role`,`agent_type`,`rate_value_percent`,`fcm_token`,`is_active`,`created_at`,`updated_at`) values 
(1,'ADM-001','Admin Manajemen','admin@integraciptadata.com','$2b$10$e806180376378272828384','081234567890','admin','internal',0.00,NULL,1,'2026-10-30 00:25:49','2026-10-30 00:25:49'),
(2,'UPL-001','Budi Santoso (Upline)','budi.upline@gmail.com','$2b$10$e806180376378272828384','081298765432','upline','external',10.00,NULL,1,'2026-10-30 00:25:49','2026-10-30 00:25:49'),
(3,'DNL-001','Ahmad Rizky (Downline)','ahmad.downline@gmail.com','$2b$10$e806180376378272828384','081311223344','downline','external',5.00,NULL,1,'2026-10-30 00:25:49','2026-10-30 00:25:49'),
(4,'DNL-002','Siti Rahma (Downline Internal)','siti.internal@gmail.com','$2b$10$e806180376378272828384','081355667788','downline','internal',0.00,NULL,1,'2026-10-30 00:25:49','2026-10-30 00:25:49');

/* Trigger structure for table `projects` */

DELIMITER $$

/*!50003 DROP TRIGGER*//*!50032 IF EXISTS */ /*!50003 `trg_calculate_commission_before_update` */$$

/*!50003 CREATE */ /*!50017 DEFINER = 'root'@'localhost' */ /*!50003 TRIGGER `trg_calculate_commission_before_update` BEFORE UPDATE ON `projects` FOR EACH ROW BEGIN
    DECLARE v_agent_type ENUM('internal', 'external');
    DECLARE v_rate_value DECIMAL(5, 2);

    -- Hanya hitung komisi jika status diubah menjadi 'deal'
    IF NEW.status = 'deal' THEN
        SELECT agent_type, rate_value_percent 
        INTO v_agent_type, v_rate_value
        FROM users 
        WHERE id = NEW.agent_id;

        -- Algoritma 3.1: Agen Internal = 0, Eksternal = Deal Value * (Rate / 100)
        IF v_agent_type = 'external' THEN
            SET NEW.calculated_commission = NEW.deal_value * (v_rate_value / 100.00);
        ELSE
            SET NEW.calculated_commission = 0.00;
        END IF;
    END IF;
END */$$


DELIMITER ;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;
