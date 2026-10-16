-- ==============================================================================
-- AquaFlow ML Leak Detection Rollback Script
-- Target: MySQL 8+
-- ==============================================================================

USE aquatrac;

SET FOREIGN_KEY_CHECKS = 0;

-- Drop newly created tables
DROP TABLE IF EXISTS `away_periods`;
DROP TABLE IF EXISTS `household_size_history`;
DROP TABLE IF EXISTS `leak_feedback`;
DROP TABLE IF EXISTS `leak_incidents`;

-- Drop new columns from existing tables
ALTER TABLE `water_usage`
  DROP COLUMN IF EXISTS `anomaly_score`,
  DROP COLUMN IF EXISTS `expected_min`,
  DROP COLUMN IF EXISTS `expected_max`,
  DROP COLUMN IF EXISTS `anomaly_reason`,
  DROP COLUMN IF EXISTS `anomaly_confidence`,
  DROP COLUMN IF EXISTS `anomaly_pattern`,
  DROP COLUMN IF EXISTS `data_quality_flag`,
  DROP COLUMN IF EXISTS `model_version`,
  DROP COLUMN IF EXISTS `scored_at`,
  DROP COLUMN IF EXISTS `incident_id`;

ALTER TABLE `apartment_occupancies`
  DROP COLUMN IF EXISTS `household_size`,
  DROP COLUMN IF EXISTS `children_count`,
  DROP COLUMN IF EXISTS `household_size_source`,
  DROP COLUMN IF EXISTS `household_size_confirmed`;

ALTER TABLE `resident_invitations`
  DROP COLUMN IF EXISTS `household_size`,
  DROP COLUMN IF EXISTS `children_count`;

ALTER TABLE `occupancy_requests`
  DROP COLUMN IF EXISTS `household_size`,
  DROP COLUMN IF EXISTS `children_count`;

SET FOREIGN_KEY_CHECKS = 1;
