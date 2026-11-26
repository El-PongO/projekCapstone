/*
SQLyog Community v13.3.1 (64 bit)
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
  `category_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci,
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `category_name` (`category_name`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

/*Data for the table `categories` */

insert  into `categories`(`id`,`category_name`,`description`,`created_at`,`updated_at`) values 
(1,'Enterprise Resource Planning (ERP)','Sistem manajemen terintegrasi proses bisnis perusahaan','2026-10-30 00:25:49','2026-10-30 00:25:49'),
(2,'Learning Management System (LMS)','Platform pembelajaran digital dan manajemen kursus','2026-10-30 00:25:49','2026-10-30 00:25:49'),
(3,'Sistem Klinik & Kesehatan','Aplikasi manajemen rekam medis dan operasional klinik','2026-10-30 00:25:49','2026-10-30 00:25:49'),
(4,'Software Development','Aplikasi dan website','2026-11-19 20:09:25','2026-11-19 20:09:25');

/*Table structure for table `clients` */

DROP TABLE IF EXISTS `clients`;

CREATE TABLE `clients` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `client_code` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  `client_name` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `phone_number` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `email` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `address` text COLLATE utf8mb4_unicode_ci,
  `created_by` bigint unsigned NOT NULL,
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_clients_code` (`client_code`),
  KEY `idx_clients_name` (`client_name`),
  KEY `idx_clients_creator` (`created_by`),
  CONSTRAINT `fk_clients_creator` FOREIGN KEY (`created_by`) REFERENCES `users` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

/*Data for the table `clients` */

insert  into `clients`(`id`,`client_code`,`client_name`,`phone_number`,`email`,`address`,`created_by`,`created_at`,`updated_at`) values 
(1,'CL-MWNX99QB','tesss','123123123123123123','tes@gmail.com','tess1',2,'2026-11-20 02:07:07','2026-11-20 02:07:07'),
(2,'CL-MWNXDDFS','tesss','123123123123123123','tes@gmail.com','tess1',2,'2026-11-20 02:10:18','2026-11-20 02:10:18'),
(3,'CL-MWNXDTLN','tesss','123123123123123123','tes@gmail.com','tess1',2,'2026-11-20 02:10:39','2026-11-20 02:10:39'),
(4,'CL-MWNXDZMS','tesss','123123123123123123','tes@gmail.com','tess1',2,'2026-11-20 02:10:47','2026-11-20 02:10:47'),
(5,'CL-MWNXGIDS','tesss','08123456789','tes@gmail.com','tess1',2,'2026-11-20 02:12:44','2026-11-20 02:12:44'),
(6,'CL-MWNXSE93','klerin','081234568790','kle@gmail.com','tunjungan',2,'2026-11-20 02:21:59','2026-11-20 02:21:59'),
(7,'CL-MWOFTFZ9','joe','12345678901234567890','joe@gmail.com','ngagel',2,'2026-11-20 10:46:41','2026-11-20 10:46:41');

/*Table structure for table `commission_payouts` */

DROP TABLE IF EXISTS `commission_payouts`;

CREATE TABLE `commission_payouts` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `commission_id` bigint unsigned NOT NULL,
  `payout_number` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  `amount_paid` decimal(15,2) NOT NULL,
  `payout_date` date NOT NULL,
  `payment_method` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  `reference_number` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `recorded_by` bigint unsigned NOT NULL,
  `notes` text COLLATE utf8mb4_unicode_ci,
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_commission_payout_number` (`payout_number`),
  KEY `idx_payouts_commission` (`commission_id`),
  KEY `idx_payouts_date` (`payout_date`),
  KEY `idx_payouts_recorded_by` (`recorded_by`),
  CONSTRAINT `fk_payouts_commission` FOREIGN KEY (`commission_id`) REFERENCES `commissions` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `fk_payouts_recorder` FOREIGN KEY (`recorded_by`) REFERENCES `users` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `chk_payout_amount` CHECK ((`amount_paid` > 0))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

/*Data for the table `commission_payouts` */

/*Table structure for table `commissions` */

DROP TABLE IF EXISTS `commissions`;

CREATE TABLE `commissions` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `project_id` bigint unsigned DEFAULT NULL,
  `agent_id` bigint unsigned DEFAULT NULL,
  `agent_type` enum('internal','external') COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `commission_rate_percent` decimal(5,2) NOT NULL DEFAULT '0.00',
  `total_commission_amount` decimal(15,2) NOT NULL DEFAULT '0.00',
  `payout_status` enum('pending','approved','paid') COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'pending',
  `payout_date` datetime DEFAULT NULL,
  `notes` text COLLATE utf8mb4_unicode_ci,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_commissions_project` (`project_id`),
  KEY `idx_commissions_agent` (`agent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

/*Data for the table `commissions` */

/*Table structure for table `follow_up_tasks` */

DROP TABLE IF EXISTS `follow_up_tasks`;

CREATE TABLE `follow_up_tasks` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `project_id` bigint unsigned DEFAULT NULL,
  `agent_id` bigint unsigned DEFAULT NULL,
  `title` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `due_at` datetime DEFAULT NULL,
  `status` enum('pending','completed','cancelled') COLLATE utf8mb4_unicode_ci DEFAULT 'pending',
  `notes` text COLLATE utf8mb4_unicode_ci,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_follow_up_project` (`project_id`),
  KEY `idx_follow_up_agent` (`agent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

/*Data for the table `follow_up_tasks` */

/*Table structure for table `invoices` */

DROP TABLE IF EXISTS `invoices`;

CREATE TABLE `invoices` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `invoice_number` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  `project_id` bigint unsigned NOT NULL,
  `created_by` bigint unsigned NOT NULL,
  `invoice_type` enum('dp','termin','final','full') COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'full',
  `total_amount` decimal(15,2) NOT NULL,
  `invoice_date` date NOT NULL,
  `due_date` date NOT NULL,
  `status` enum('unpaid','partial','paid') COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'unpaid',
  `notes` text COLLATE utf8mb4_unicode_ci,
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_invoices_number` (`invoice_number`),
  KEY `idx_invoices_project` (`project_id`),
  KEY `idx_invoices_creator` (`created_by`),
  KEY `idx_invoices_status_due` (`status`,`due_date`),
  CONSTRAINT `fk_invoices_creator` FOREIGN KEY (`created_by`) REFERENCES `users` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `fk_invoices_project` FOREIGN KEY (`project_id`) REFERENCES `projects` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `chk_invoice_amount` CHECK ((`total_amount` > 0)),
  CONSTRAINT `chk_invoice_due_date` CHECK ((`due_date` >= `invoice_date`))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

/*Data for the table `invoices` */

/*Table structure for table `items` */

DROP TABLE IF EXISTS `items`;

CREATE TABLE `items` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `category_id` bigint unsigned NOT NULL,
  `item_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `item_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `base_price` decimal(15,2) NOT NULL DEFAULT '0.00',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci,
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `item_code` (`item_code`),
  KEY `fk_items_category` (`category_id`),
  CONSTRAINT `fk_items_category` FOREIGN KEY (`category_id`) REFERENCES `categories` (`id`) ON DELETE RESTRICT
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

/*Data for the table `items` */

insert  into `items`(`id`,`category_id`,`item_code`,`item_name`,`base_price`,`description`,`created_at`,`updated_at`) values 
(1,1,'ITM-ERP-01','Core ERP Module System',75000000.00,'Modul utama keuangan, inventaris, dan SDM','2026-10-30 00:25:49','2026-10-30 00:25:49'),
(2,2,'ITM-LMS-01','Standard LMS Platform',35000000.00,'Sistem manajemen ujian dan kelas daring','2026-10-30 00:25:49','2026-10-30 00:25:49'),
(3,3,'ITM-CLN-01','Clinic Management System',25000000.00,'Sistem pendaftaran, kasir, dan rekam medis pasien','2026-10-30 00:25:49','2026-10-30 00:25:49'),
(4,4,'ITM-001','Pembuatan Website',15000000.00,NULL,'2026-11-19 20:09:25','2026-11-19 20:09:25');

/*Table structure for table `meetings` */

DROP TABLE IF EXISTS `meetings`;

CREATE TABLE `meetings` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `project_id` bigint unsigned NOT NULL,
  `agent_id` bigint unsigned NOT NULL,
  `meeting_title` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `meeting_at` datetime NOT NULL,
  `meeting_end_at` datetime DEFAULT NULL,
  `location` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `agenda` text COLLATE utf8mb4_unicode_ci,
  `status` enum('scheduled','completed','cancelled') COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'scheduled',
  `notes` text COLLATE utf8mb4_unicode_ci,
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_meetings_project` (`project_id`),
  KEY `idx_meetings_agent_date` (`agent_id`,`meeting_at`),
  CONSTRAINT `fk_meetings_agent` FOREIGN KEY (`agent_id`) REFERENCES `users` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `fk_meetings_project` FOREIGN KEY (`project_id`) REFERENCES `projects` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `chk_meeting_time` CHECK (((`meeting_end_at` is null) or (`meeting_end_at` > `meeting_at`)))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

/*Data for the table `meetings` */

/*Table structure for table `notifications` */

DROP TABLE IF EXISTS `notifications`;

CREATE TABLE `notifications` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `user_id` bigint unsigned DEFAULT NULL,
  `title` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `message` text COLLATE utf8mb4_unicode_ci,
  `type` enum('project_status','commission','broadcast','system') COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `is_read` tinyint(1) NOT NULL DEFAULT '0',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_notifications_user` (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=13 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

/*Data for the table `notifications` */

insert  into `notifications`(`id`,`user_id`,`title`,`message`,`type`,`is_read`,`created_at`) values 
(1,1,'tes','heloo geas','broadcast',1,'2026-11-20 06:30:04'),
(2,2,'tes','heloo geas','broadcast',1,'2026-11-20 06:30:04'),
(3,3,'tes','heloo geas','broadcast',1,'2026-11-20 06:30:04'),
(4,4,'tes','heloo geas','broadcast',0,'2026-11-20 06:30:04'),
(5,1,'helooo gais jangna lupa dedlennya','hiyaaaa','broadcast',1,'2026-11-20 08:47:33'),
(6,2,'helooo gais jangna lupa dedlennya','hiyaaaa','broadcast',1,'2026-11-20 08:47:33'),
(7,3,'helooo gais jangna lupa dedlennya','hiyaaaa','broadcast',1,'2026-11-20 08:47:33'),
(8,4,'helooo gais jangna lupa dedlennya','hiyaaaa','broadcast',0,'2026-11-20 08:47:33'),
(9,1,'tess anonce','projeknya jgn lupa','broadcast',0,'2026-11-20 10:52:36'),
(10,2,'tess anonce','projeknya jgn lupa','broadcast',0,'2026-11-20 10:52:36'),
(11,3,'tess anonce','projeknya jgn lupa','broadcast',0,'2026-11-20 10:52:36'),
(12,4,'tess anonce','projeknya jgn lupa','broadcast',0,'2026-11-20 10:52:36');

/*Table structure for table `payments` */

DROP TABLE IF EXISTS `payments`;

CREATE TABLE `payments` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `project_id` bigint unsigned NOT NULL,
  `invoice_id` bigint unsigned DEFAULT NULL,
  `payment_receipt_no` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `amount_paid` decimal(15,2) NOT NULL DEFAULT '0.00',
  `payment_date` date NOT NULL,
  `payment_method` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `payment_status` enum('unpaid','partial','paid') CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'unpaid',
  `due_date` date NOT NULL,
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `payment_receipt_no` (`payment_receipt_no`),
  KEY `fk_payments_project` (`project_id`),
  KEY `idx_payment_status` (`payment_status`),
  KEY `idx_due_date` (`due_date`),
  KEY `idx_payments_invoice` (`invoice_id`),
  CONSTRAINT `fk_payments_invoice` FOREIGN KEY (`invoice_id`) REFERENCES `invoices` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `fk_payments_project` FOREIGN KEY (`project_id`) REFERENCES `projects` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

/*Data for the table `payments` */

insert  into `payments`(`id`,`project_id`,`invoice_id`,`payment_receipt_no`,`amount_paid`,`payment_date`,`payment_method`,`payment_status`,`due_date`,`created_at`) values 
(1,1,NULL,'PAY-2026-001',10000000.00,'2026-09-10','BCA Virtual Account','partial','2026-10-10','2026-10-30 00:25:49');

/*Table structure for table `project_activity_logs` */

DROP TABLE IF EXISTS `project_activity_logs`;

CREATE TABLE `project_activity_logs` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `project_id` bigint unsigned NOT NULL,
  `user_id` bigint unsigned NOT NULL,
  `activity_description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `previous_stage` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `current_stage` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `fk_activity_project` (`project_id`),
  KEY `fk_activity_user` (`user_id`),
  CONSTRAINT `fk_activity_project` FOREIGN KEY (`project_id`) REFERENCES `projects` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_activity_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE RESTRICT
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

/*Data for the table `project_activity_logs` */

insert  into `project_activity_logs`(`id`,`project_id`,`user_id`,`activity_description`,`previous_stage`,`current_stage`,`created_at`) values 
(1,1,3,'Menyelesaikan pembayaran DP sebesar Rp 10.000.000','proposal_sent','implementation','2026-10-30 00:25:49'),
(2,8,2,'Proyek dibuat',NULL,'negotiation','2026-11-20 02:22:00'),
(3,9,2,'Proyek dibuat',NULL,'negotiation','2026-11-20 10:46:41');

/*Table structure for table `project_approvals` */

DROP TABLE IF EXISTS `project_approvals`;

CREATE TABLE `project_approvals` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `project_id` bigint unsigned DEFAULT NULL,
  `requested_by` bigint unsigned DEFAULT NULL,
  `reviewed_by` bigint unsigned DEFAULT NULL,
  `status` enum('pending','approved','rejected') COLLATE utf8mb4_unicode_ci DEFAULT 'pending',
  `reason` text COLLATE utf8mb4_unicode_ci,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `reviewed_at` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_project_approvals_project` (`project_id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

/*Data for the table `project_approvals` */

insert  into `project_approvals`(`id`,`project_id`,`requested_by`,`reviewed_by`,`status`,`reason`,`created_at`,`reviewed_at`) values 
(1,8,2,NULL,'pending',NULL,'2026-11-20 02:36:13',NULL);

/*Table structure for table `project_documents` */

DROP TABLE IF EXISTS `project_documents`;

CREATE TABLE `project_documents` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `project_id` bigint unsigned DEFAULT NULL,
  `uploaded_by` bigint unsigned DEFAULT NULL,
  `document_type` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `original_filename` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `file_path` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_project_documents_project` (`project_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

/*Data for the table `project_documents` */

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
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

/*Data for the table `project_items` */

insert  into `project_items`(`id`,`project_id`,`item_id`,`quantity`,`price`,`created_at`) values 
(1,1,3,1,25000000.00,'2026-10-30 00:25:49'),
(2,2,1,1,75000000.00,'2026-10-30 00:25:49'),
(8,8,1,1,100.00,'2026-11-20 02:22:00'),
(9,9,3,1,10000.00,'2026-11-20 10:46:41');

/*Table structure for table `project_milestones` */

DROP TABLE IF EXISTS `project_milestones`;

CREATE TABLE `project_milestones` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `project_id` bigint unsigned DEFAULT NULL,
  `title` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `description` text COLLATE utf8mb4_unicode_ci,
  `status` enum('pending','in_progress','completed') COLLATE utf8mb4_unicode_ci DEFAULT 'pending',
  `due_date` date DEFAULT NULL,
  `completed_at` datetime DEFAULT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_milestones_project` (`project_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

/*Data for the table `project_milestones` */

/*Table structure for table `projects` */

DROP TABLE IF EXISTS `projects`;

CREATE TABLE `projects` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `project_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `agent_id` bigint unsigned NOT NULL,
  `client_id` bigint unsigned DEFAULT NULL,
  `client_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `client_phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `client_email` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `status` enum('follow_up','deal','no_deal','cancelled') CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'follow_up',
  `deal_date` date DEFAULT NULL,
  `execution_stage` enum('negotiation','proposal_sent','app_creation','implementation','completed') CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'negotiation',
  `deal_value` decimal(15,2) NOT NULL DEFAULT '0.00',
  `calculated_commission` decimal(15,2) NOT NULL DEFAULT '0.00',
  `cancel_reason` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci,
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `project_code` (`project_code`),
  KEY `fk_projects_agent` (`agent_id`),
  KEY `idx_project_status` (`status`),
  KEY `idx_execution_stage` (`execution_stage`),
  KEY `idx_projects_client` (`client_id`),
  CONSTRAINT `fk_projects_agent` FOREIGN KEY (`agent_id`) REFERENCES `users` (`id`) ON DELETE RESTRICT,
  CONSTRAINT `fk_projects_client` FOREIGN KEY (`client_id`) REFERENCES `clients` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

/*Data for the table `projects` */

insert  into `projects`(`id`,`project_code`,`agent_id`,`client_id`,`client_name`,`client_phone`,`client_email`,`status`,`deal_date`,`execution_stage`,`deal_value`,`calculated_commission`,`cancel_reason`,`created_at`,`updated_at`) values 
(1,'PRJ-2026-001',3,NULL,'Klinik Sehat Bersama','081999888777','info@sehatbersama.com','deal',NULL,'implementation',25000000.00,0.00,NULL,'2026-10-30 00:25:49','2026-10-30 00:25:49'),
(2,'PRJ-2026-002',2,NULL,'PT Trans Nusantara','081777666555','procurement@transnusantara.com','follow_up',NULL,'negotiation',75000000.00,0.00,NULL,'2026-10-30 00:25:49','2026-10-30 00:25:49'),
(8,'PRJ-MWNXSEZA-929266',2,6,'klerin','081234568790','kle@gmail.com','follow_up',NULL,'negotiation',100.00,0.00,NULL,'2026-11-20 02:22:00','2026-11-20 02:22:00'),
(9,'PRJ-MWOFTGLU-E95720',2,7,'joe','12345678901234567890','joe@gmail.com','follow_up',NULL,'negotiation',10000.00,0.00,NULL,'2026-11-20 10:46:41','2026-11-20 10:46:41');

/*Table structure for table `proposal_items` */

DROP TABLE IF EXISTS `proposal_items`;

CREATE TABLE `proposal_items` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `proposal_id` bigint unsigned NOT NULL,
  `item_id` bigint unsigned DEFAULT NULL,
  `item_name` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `description` text COLLATE utf8mb4_unicode_ci,
  `quantity` int unsigned NOT NULL DEFAULT '1',
  `unit_price` decimal(15,2) NOT NULL DEFAULT '0.00',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_proposal_items_proposal` (`proposal_id`),
  KEY `idx_proposal_items_item` (`item_id`),
  CONSTRAINT `fk_proposal_items_item` FOREIGN KEY (`item_id`) REFERENCES `items` (`id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `fk_proposal_items_proposal` FOREIGN KEY (`proposal_id`) REFERENCES `proposals` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `chk_proposal_item_price` CHECK ((`unit_price` >= 0)),
  CONSTRAINT `chk_proposal_item_quantity` CHECK ((`quantity` > 0))
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

/*Data for the table `proposal_items` */

insert  into `proposal_items`(`id`,`proposal_id`,`item_id`,`item_name`,`description`,`quantity`,`unit_price`,`created_at`) values 
(1,1,1,'Core ERP Module System','hehehehehehhehee',1,100.00,'2026-11-20 02:22:01'),
(2,2,NULL,'Clinic Management System','Menyelesaikan pembayaran DP sebesar Rp 10.000.000',1,25000000.00,'2026-11-20 10:10:10'),
(3,3,3,'Clinic Management System','tess dtail',1,10000.00,'2026-11-20 10:46:42');

/*Table structure for table `proposals` */

DROP TABLE IF EXISTS `proposals`;

CREATE TABLE `proposals` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `proposal_number` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `project_id` bigint unsigned DEFAULT NULL,
  `agent_id` bigint unsigned DEFAULT NULL,
  `proposal_title` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `pdf_file_path` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `valid_until` date DEFAULT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_proposals_project` (`project_id`),
  KEY `idx_proposals_agent` (`agent_id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

/*Data for the table `proposals` */

insert  into `proposals`(`id`,`proposal_number`,`project_id`,`agent_id`,`proposal_title`,`pdf_file_path`,`valid_until`,`created_at`) values 
(1,'PRP-MWNXSFEV-EE720B',8,2,'Proposal klerin','proposals/PRP-MWNXSFEV-EE720B.pdf',NULL,'2026-11-20 02:22:01'),
(2,'PRP-MWOEIGZH-D9C739',1,3,'Proposal Klinik Sehat Bersama','proposals/PRP-MWOEIGZH-D9C739.pdf',NULL,'2026-11-20 10:10:09'),
(3,'PRP-MWOFTGTL-2043BB',9,2,'Proposal joe','proposals/PRP-MWOFTGTL-2043BB.pdf',NULL,'2026-11-20 10:46:42');

/*Table structure for table `users` */

DROP TABLE IF EXISTS `users`;

CREATE TABLE `users` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `agent_code` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `email` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `password` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `phone_number` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `role` enum('admin','upline','downline') CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `agent_type` enum('internal','external') CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'external',
  `rate_value_percent` decimal(5,2) NOT NULL DEFAULT '0.00',
  `fcm_token` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci,
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
(1,'ADM-001','Admin Manajemen','admin@integraciptadata.com','$2b$10$uDn30BrnUPAo7xvKrrFGVunJk8BEUHi/8KL5ZRzU0qAf5kANDKhDG','081234567890','admin','internal',0.00,NULL,1,'2026-10-30 00:25:49','2026-11-20 10:52:55'),
(2,'UPL-001','Budi Santoso (Upline)','budi.upline@gmail.com','$2b$10$uDn30BrnUPAo7xvKrrFGVunJk8BEUHi/8KL5ZRzU0qAf5kANDKhDG','081298765432','upline','external',10.00,NULL,1,'2026-10-30 00:25:49','2026-11-20 10:54:00'),
(3,'DNL-001','Ahmad Rizky (Downline)','ahmad.downline@gmail.com','$2b$10$uDn30BrnUPAo7xvKrrFGVunJk8BEUHi/8KL5ZRzU0qAf5kANDKhDG','081311223344','downline','external',5.00,'dzVZv673T9aR6kxea5ftIb:APA91bHMk4OuIizGWM4ppmvufx3Kvig9qCfHPRnIOd5FYdBwobzDtaRRDOnW46KHxf7wNXfDxEJlbvmmrdwtoR9Lp-2VhscsHsdd3Btc5yBGh4RHJhitMNw',1,'2026-10-30 00:25:49','2026-11-20 10:54:16'),
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
