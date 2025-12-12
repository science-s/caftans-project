-- Schéma MySQL pour l'application de location de caftans
-- Importez ce fichier dans phpMyAdmin (ou via mysql CLI) après avoir créé la base `caftans_db`

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

CREATE TABLE IF NOT EXISTS `users` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `email` VARCHAR(120) NOT NULL,
  `password_hash` VARCHAR(255) NOT NULL,
  `full_name` VARCHAR(100),
  `phone` VARCHAR(20),
  `role` VARCHAR(20) NOT NULL DEFAULT 'user',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `ux_users_email` (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `categories` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(100) NOT NULL,
  `description` TEXT,
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `ux_categories_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `caftans` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `category_id` BIGINT NOT NULL,
  `name` VARCHAR(200) NOT NULL,
  `description` TEXT,
  `price_per_day` DECIMAL(10,2) NOT NULL,
  `availability_status` VARCHAR(20) NOT NULL DEFAULT 'available',
  `image_url` VARCHAR(500),
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_caftans_category` (`category_id`),
  CONSTRAINT `fk_caftans_category` FOREIGN KEY (`category_id`) REFERENCES `categories` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `reservations` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL,
  `caftan_id` BIGINT NOT NULL,
  `start_date` DATE NOT NULL,
  `end_date` DATE NOT NULL,
  `status` VARCHAR(20) NOT NULL DEFAULT 'pending',
  `notes` TEXT,
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_res_user` (`user_id`),
  KEY `idx_res_caftan` (`caftan_id`),
  CONSTRAINT `fk_res_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_res_caftan` FOREIGN KEY (`caftan_id`) REFERENCES `caftans` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `favorites` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL,
  `caftan_id` BIGINT NOT NULL,
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `unique_user_caftan` (`user_id`,`caftan_id`),
  CONSTRAINT `fk_fav_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_fav_caftan` FOREIGN KEY (`caftan_id`) REFERENCES `caftans` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Données de base (catégories et exemples de caftans)
INSERT INTO `categories` (`name`, `description`) VALUES
  ('Espace Mariage', 'Caftans élégants pour les mariages'),
  ('Espace Fête', 'Caftans festifs pour les occasions spéciales'),
  ('Espace Traditionnel', 'Caftans traditionnels marocains'),
  ('Espace Moderne', 'Caftans modernes et tendance');

INSERT INTO `caftans` (`category_id`, `name`, `description`, `price_per_day`, `availability_status`, `image_url`) VALUES
  (1, 'Caftan Mariage Doré', 'Soie dorée avec broderies traditionnelles', 500.00, 'available', NULL),
  (1, 'Caftan Mariage Blanc Perle', 'Blanc perle avec perles et sequins', 600.00, 'available', NULL),
  (2, 'Caftan Fête Bleu Royal', 'Festif bleu royal avec motifs géométriques', 300.00, 'available', NULL),
  (3, 'Caftan Traditionnel Vert', 'Vert émeraude avec broderies manuelles', 250.00, 'available', NULL),
  (4, 'Caftan Moderne Noir', 'Coupe contemporaine noire', 350.00, 'available', NULL),
  (4, 'Caftan Moderne Rose', 'Rose poudré avec finitions élégantes', 320.00, 'available', NULL);

SET FOREIGN_KEY_CHECKS = 1;

