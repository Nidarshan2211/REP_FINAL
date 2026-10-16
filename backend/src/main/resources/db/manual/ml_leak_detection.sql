-- ==============================================================================
-- AquaFlow ML Leak Detection & Household Size Data Capture Migration Script
-- Target: MySQL 8+
-- ==============================================================================

USE aquatrac;

-- ------------------------------------------------------------------------------
-- 1. Extend water_usage Table with ML Scoring Columns
-- ------------------------------------------------------------------------------
ALTER TABLE `water_usage`
  ADD COLUMN `anomaly_score` DOUBLE NULL,
  ADD COLUMN `expected_min` DOUBLE NULL,
  ADD COLUMN `expected_max` DOUBLE NULL,
  ADD COLUMN `anomaly_reason` VARCHAR(500) NULL,
  ADD COLUMN `anomaly_confidence` VARCHAR(20) NULL,
  ADD COLUMN `anomaly_pattern` VARCHAR(30) NULL,
  ADD COLUMN `data_quality_flag` VARCHAR(30) NULL,
  ADD COLUMN `model_version` VARCHAR(30) NULL,
  ADD COLUMN `scored_at` DATETIME(6) NULL,
  ADD COLUMN `incident_id` BIGINT NULL;

-- Index for high-frequency queries by meter and billing cycle
ALTER TABLE `water_usage`
  ADD INDEX `idx_water_usage_meter_cycle` (`meter_id`, `billing_cycle_id`);

-- ------------------------------------------------------------------------------
-- 2. Extend apartment_occupancies Table with Household Size
-- ------------------------------------------------------------------------------
ALTER TABLE `apartment_occupancies`
  ADD COLUMN `household_size` INT NULL,
  ADD COLUMN `children_count` INT NULL,
  ADD COLUMN `household_size_source` VARCHAR(30) NULL,
  ADD COLUMN `household_size_confirmed` BOOLEAN NOT NULL DEFAULT FALSE;

-- ------------------------------------------------------------------------------
-- 3. Extend resident_invitations & occupancy_requests Tables
-- ------------------------------------------------------------------------------
ALTER TABLE `resident_invitations`
  ADD COLUMN `household_size` INT NULL,
  ADD COLUMN `children_count` INT NULL;

ALTER TABLE `occupancy_requests`
  ADD COLUMN `household_size` INT NULL,
  ADD COLUMN `children_count` INT NULL;

-- ------------------------------------------------------------------------------
-- 4. Create leak_incidents Table
-- ------------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `leak_incidents` (
  `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
  `apartment_id` BIGINT NOT NULL,
  `first_usage_id` BIGINT NOT NULL,
  `last_usage_id` BIGINT NOT NULL,
  `status` VARCHAR(30) NOT NULL DEFAULT 'OPEN',
  `peak_risk_score` DOUBLE NOT NULL,
  `opened_at` DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  `resolved_at` DATETIME(6) NULL,
  KEY `idx_incident_apartment` (`apartment_id`),
  KEY `idx_incident_status` (`status`),
  CONSTRAINT `fk_incident_apartment` FOREIGN KEY (`apartment_id`) REFERENCES `apartments` (`apartment_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ------------------------------------------------------------------------------
-- 5. Create leak_feedback Table
-- ------------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `leak_feedback` (
  `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
  `usage_id` BIGINT NOT NULL,
  `apartment_id` BIGINT NOT NULL,
  `submitted_by_user_id` BIGINT NOT NULL,
  `submitter_role` VARCHAR(50) NOT NULL,
  `feedback` VARCHAR(50) NOT NULL,
  `note` VARCHAR(255) NULL,
  `created_at` DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  KEY `idx_feedback_usage` (`usage_id`),
  KEY `idx_feedback_apartment` (`apartment_id`),
  KEY `idx_feedback_user` (`submitted_by_user_id`),
  CONSTRAINT `fk_feedback_usage` FOREIGN KEY (`usage_id`) REFERENCES `water_usage` (`usage_id`) ON DELETE CASCADE,
  CONSTRAINT `fk_feedback_apartment` FOREIGN KEY (`apartment_id`) REFERENCES `apartments` (`apartment_id`) ON DELETE CASCADE,
  CONSTRAINT `fk_feedback_user` FOREIGN KEY (`submitted_by_user_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ------------------------------------------------------------------------------
-- 6. Create household_size_history Table
-- ------------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `household_size_history` (
  `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
  `occupancy_id` BIGINT NOT NULL,
  `apartment_id` BIGINT NOT NULL,
  `household_size` INT NOT NULL,
  `children_count` INT NULL,
  `effective_from` DATE NOT NULL,
  `effective_to` DATE NULL,
  `changed_by_user_id` BIGINT NULL,
  `changed_by_role` VARCHAR(50) NOT NULL,
  `reason` VARCHAR(50) NOT NULL,
  `note` VARCHAR(255) NULL,
  `sanity_warning` BOOLEAN DEFAULT FALSE,
  `status` VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',
  `created_at` DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  KEY `idx_hh_hist_apt_from` (`apartment_id`, `effective_from`),
  KEY `idx_hh_hist_occupancy` (`occupancy_id`),
  CONSTRAINT `fk_hh_hist_occupancy` FOREIGN KEY (`occupancy_id`) REFERENCES `apartment_occupancies` (`occupancy_id`) ON DELETE CASCADE,
  CONSTRAINT `fk_hh_hist_apartment` FOREIGN KEY (`apartment_id`) REFERENCES `apartments` (`apartment_id`) ON DELETE CASCADE,
  CONSTRAINT `fk_hh_hist_user` FOREIGN KEY (`changed_by_user_id`) REFERENCES `users` (`user_id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ------------------------------------------------------------------------------
-- 7. Create away_periods Table
-- ------------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `away_periods` (
  `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
  `occupancy_id` BIGINT NOT NULL,
  `apartment_id` BIGINT NOT NULL,
  `start_date` DATE NOT NULL,
  `end_date` DATE NOT NULL,
  `note` VARCHAR(255) NULL,
  `created_at` DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  KEY `idx_away_occupancy` (`occupancy_id`),
  KEY `idx_away_apartment` (`apartment_id`),
  CONSTRAINT `fk_away_occupancy` FOREIGN KEY (`occupancy_id`) REFERENCES `apartment_occupancies` (`occupancy_id`) ON DELETE CASCADE,
  CONSTRAINT `fk_away_apartment` FOREIGN KEY (`apartment_id`) REFERENCES `apartments` (`apartment_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ------------------------------------------------------------------------------
-- 8. Backfill Script for Existing Occupancies
-- ------------------------------------------------------------------------------
-- Populate household_size defaults based on unit_type: STUDIO:1, 1BHK:2, 2BHK:3, 3BHK:4, PENTHOUSE:5 (default: 3)
UPDATE `apartment_occupancies` occ
JOIN `apartments` apt ON occ.`apartment_id` = apt.`apartment_id`
SET
  occ.`household_size` = CASE
    WHEN UPPER(apt.`unit_type`) = 'STUDIO' THEN 1
    WHEN UPPER(apt.`unit_type`) = '1BHK' THEN 2
    WHEN UPPER(apt.`unit_type`) = '2BHK' THEN 3
    WHEN UPPER(apt.`unit_type`) = '3BHK' THEN 4
    WHEN UPPER(apt.`unit_type`) = 'PENTHOUSE' THEN 5
    ELSE 3
  END,
  occ.`children_count` = 0,
  occ.`household_size_source` = 'DEFAULT_ESTIMATE',
  occ.`household_size_confirmed` = FALSE
WHERE occ.`household_size` IS NULL;

-- Insert initial baseline record into household_size_history for existing active occupancies
INSERT INTO `household_size_history` (
  `occupancy_id`,
  `apartment_id`,
  `household_size`,
  `children_count`,
  `effective_from`,
  `effective_to`,
  `changed_by_user_id`,
  `changed_by_role`,
  `reason`,
  `note`,
  `sanity_warning`,
  `status`,
  `created_at`
)
SELECT
  occ.`occupancy_id`,
  occ.`apartment_id`,
  COALESCE(occ.`household_size`, 3),
  COALESCE(occ.`children_count`, 0),
  DATE(COALESCE(occ.`start_date`, NOW())),
  NULL,
  res.`user_id`,
  'SYSTEM',
  'MOVE_IN',
  'Automatic backfill of default household estimate for legacy tenancy',
  FALSE,
  'ACTIVE',
  NOW()
FROM `apartment_occupancies` occ
JOIN `residents` res ON occ.`resident_id` = res.`resident_id`
WHERE NOT EXISTS (
  SELECT 1 FROM `household_size_history` hsh WHERE hsh.`occupancy_id` = occ.`occupancy_id`
);
