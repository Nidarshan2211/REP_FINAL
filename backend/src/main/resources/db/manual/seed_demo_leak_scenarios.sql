-- ============================================================================
-- AquaFlow Demo Seed Script: Sukimiya Apartment & Section 12 Leak Scenarios
-- ============================================================================
-- Owner: owner@example.com
-- Building: Sukimiya Apartment (SUK-01)
-- 5 Walkthrough Flats:
--   Flat 101: Baseline Normal (3 people, steady 12-14 kL, current 13.2 kL -> NORMAL)
--   Flat 204: Catastrophic Spike (4 people, steady 16 kL, current 48.5 kL -> POTENTIAL_LEAK)
--   Flat 302: Silent Toilet Flapper Trickle (2 people, steady 9 kL, current 18.7 kL -> POTENTIAL_LEAK)
--   Flat 405: Vacant / Away Period (3 people, normal 14 kL drops to 1.2 kL with 18 away days -> NORMAL)
--   Flat 501: Multi-Cycle Creep (3 people, 12 kL -> 17 kL -> 23 kL -> POTENTIAL_LEAK, linked incident)
-- ============================================================================

SET FOREIGN_KEY_CHECKS = 0;

-- 1. Create or ensure Owner User (owner@example.com)
INSERT INTO users (email, name, password, role, status, created_at)
VALUES (
    'owner@example.com',
    'Sukimiya Owner',
    '$2a$10$9Vc.t17Xi2H2OUKuFN.6O.rqQVE5NDwwa4s.2cVKOilRHu5qTC5Oq', -- Password@123
    'BUILDING_OWNER',
    'ACTIVE',
    NOW()
)
ON DUPLICATE KEY UPDATE name = 'Sukimiya Owner', status = 'ACTIVE';

SET @owner_user_id = (SELECT user_id FROM users WHERE email = 'owner@example.com');

-- 2. Create BuildingOwner entity
INSERT INTO building_owners (user_id, name, phone, approval_status, created_at)
VALUES (
    @owner_user_id,
    'Sukimiya Owner',
    '9876500000',
    'APPROVED',
    NOW()
)
ON DUPLICATE KEY UPDATE approval_status = 'APPROVED';

SET @owner_id = (SELECT owner_id FROM building_owners WHERE user_id = @owner_user_id);

-- 3. Create Sukimiya Apartment Building
INSERT INTO buildings (building_name, building_number, location, owner_id, total_flats, created_at)
VALUES (
    'Sukimiya Apartment',
    'SUK-01',
    '7th Main Road, Indiranagar, Bangalore',
    @owner_id,
    5,
    NOW()
)
ON DUPLICATE KEY UPDATE total_flats = 5;

SET @bldg_id = (SELECT building_id FROM buildings WHERE building_name = 'Sukimiya Apartment' LIMIT 1);

-- 4. Create Billing Cycles (6 months history + current cycle)
-- Cycles: May 2026 to October 2026 (Month 5 to Month 10)
INSERT INTO billing_cycles (building_id, billing_month, start_date, end_date, status, created_at)
VALUES
    (@bldg_id, '2026-05-01', '2026-05-01', '2026-05-31', 'FINALIZED', '2026-05-31 23:59:59'),
    (@bldg_id, '2026-06-01', '2026-06-01', '2026-06-30', 'FINALIZED', '2026-06-30 23:59:59'),
    (@bldg_id, '2026-07-01', '2026-07-01', '2026-07-31', 'FINALIZED', '2026-07-31 23:59:59'),
    (@bldg_id, '2026-08-01', '2026-08-01', '2026-08-31', 'FINALIZED', '2026-08-31 23:59:59'),
    (@bldg_id, '2026-09-01', '2026-09-01', '2026-09-30', 'FINALIZED', '2026-09-30 23:59:59'),
    (@bldg_id, '2026-10-01', '2026-10-01', '2026-10-31', 'OPEN',      '2026-10-01 00:00:00')
ON DUPLICATE KEY UPDATE status = VALUES(status);

SET @cycle_may = (SELECT id FROM billing_cycles WHERE building_id = @bldg_id AND billing_month = '2026-05-01' LIMIT 1);
SET @cycle_jun = (SELECT id FROM billing_cycles WHERE building_id = @bldg_id AND billing_month = '2026-06-01' LIMIT 1);
SET @cycle_jul = (SELECT id FROM billing_cycles WHERE building_id = @bldg_id AND billing_month = '2026-07-01' LIMIT 1);
SET @cycle_aug = (SELECT id FROM billing_cycles WHERE building_id = @bldg_id AND billing_month = '2026-08-01' LIMIT 1);
SET @cycle_sep = (SELECT id FROM billing_cycles WHERE building_id = @bldg_id AND billing_month = '2026-09-01' LIMIT 1);
SET @cycle_oct = (SELECT id FROM billing_cycles WHERE building_id = @bldg_id AND billing_month = '2026-10-01' LIMIT 1);

-- 5. Helper procedure / statements to insert flats & tenancies
-- Flat 101 (2BHK, 3 people, Baseline normal)
INSERT INTO apartments (building_id, room_number, floor_number, unit_type, apartment_name, created_at)
VALUES (@bldg_id, '101', 1, '2BHK', 'Flat 101', NOW())
ON DUPLICATE KEY UPDATE apartment_name = 'Flat 101';
SET @apt_101 = (SELECT apartment_id FROM apartments WHERE building_id = @bldg_id AND room_number = '101' LIMIT 1);

INSERT INTO users (email, name, password, role, status, created_at)
VALUES ('res101@example.com', 'Arun Kumar', '$2a$10$9Vc.t17Xi2H2OUKuFN.6O.rqQVE5NDwwa4s.2cVKOilRHu5qTC5Oq', 'RESIDENT', 'ACTIVE', NOW())
ON DUPLICATE KEY UPDATE name = 'Arun Kumar';
SET @user_101 = (SELECT user_id FROM users WHERE email = 'res101@example.com' LIMIT 1);

INSERT INTO residents (user_id, building_id, name, email, phone, apartment_number, invitation_status, account_status, created_at)
VALUES (@user_101, @bldg_id, 'Arun Kumar', 'res101@example.com', '9876500101', '101', 'ACCEPTED', 'ACTIVE', NOW())
ON DUPLICATE KEY UPDATE name = 'Arun Kumar';
SET @res_101 = (SELECT resident_id FROM residents WHERE email = 'res101@example.com' LIMIT 1);

INSERT INTO water_meters (building_id, apartment_id, resident_id, meter_number, installation_date, status)
VALUES (@bldg_id, @apt_101, @res_101, 'WM-SUK-101', '2025-12-01', 'ACTIVE')
ON DUPLICATE KEY UPDATE status = 'ACTIVE';
SET @meter_101 = (SELECT meter_id FROM water_meters WHERE meter_number = 'WM-SUK-101' LIMIT 1);

INSERT INTO apartment_occupancies (apartment_id, resident_id, start_date, status, household_size, children_count, household_size_source, household_size_confirmed, created_at)
VALUES (@apt_101, @res_101, '2026-01-01', 'ACTIVE', 3, 1, 'RESIDENT_DECLARED', TRUE, NOW())
ON DUPLICATE KEY UPDATE household_size = 3, household_size_confirmed = TRUE;
SET @occ_101 = (SELECT occupancy_id FROM apartment_occupancies WHERE apartment_id = @apt_101 AND status = 'ACTIVE' LIMIT 1);

INSERT INTO household_size_history (occupancy_id, apartment_id, household_size, children_count, effective_from, changed_by_user_id, changed_by_role, reason, status, created_at)
VALUES (@occ_101, @apt_101, 3, 1, '2026-01-01', @user_101, 'RESIDENT', 'MOVE_IN', 'ACTIVE', NOW());

-- Flat 204 (3BHK, 4 people, Catastrophic Spike 48.5 kL)
INSERT INTO apartments (building_id, room_number, floor_number, unit_type, apartment_name, created_at)
VALUES (@bldg_id, '204', 2, '3BHK', 'Flat 204', NOW())
ON DUPLICATE KEY UPDATE apartment_name = 'Flat 204';
SET @apt_204 = (SELECT apartment_id FROM apartments WHERE building_id = @bldg_id AND room_number = '204' LIMIT 1);

INSERT INTO users (email, name, password, role, status, created_at)
VALUES ('res204@example.com', 'Priya Sharma', '$2a$10$9Vc.t17Xi2H2OUKuFN.6O.rqQVE5NDwwa4s.2cVKOilRHu5qTC5Oq', 'RESIDENT', 'ACTIVE', NOW())
ON DUPLICATE KEY UPDATE name = 'Priya Sharma';
SET @user_204 = (SELECT user_id FROM users WHERE email = 'res204@example.com' LIMIT 1);

INSERT INTO residents (user_id, building_id, name, email, phone, apartment_number, invitation_status, account_status, created_at)
VALUES (@user_204, @bldg_id, 'Priya Sharma', 'res204@example.com', '9876500204', '204', 'ACCEPTED', 'ACTIVE', NOW())
ON DUPLICATE KEY UPDATE name = 'Priya Sharma';
SET @res_204 = (SELECT resident_id FROM residents WHERE email = 'res204@example.com' LIMIT 1);

INSERT INTO water_meters (building_id, apartment_id, resident_id, meter_number, installation_date, status)
VALUES (@bldg_id, @apt_204, @res_204, 'WM-SUK-204', '2025-12-01', 'ACTIVE')
ON DUPLICATE KEY UPDATE status = 'ACTIVE';
SET @meter_204 = (SELECT meter_id FROM water_meters WHERE meter_number = 'WM-SUK-204' LIMIT 1);

INSERT INTO apartment_occupancies (apartment_id, resident_id, start_date, status, household_size, children_count, household_size_source, household_size_confirmed, created_at)
VALUES (@apt_204, @res_204, '2026-01-01', 'ACTIVE', 4, 1, 'RESIDENT_DECLARED', TRUE, NOW())
ON DUPLICATE KEY UPDATE household_size = 4, household_size_confirmed = TRUE;
SET @occ_204 = (SELECT occupancy_id FROM apartment_occupancies WHERE apartment_id = @apt_204 AND status = 'ACTIVE' LIMIT 1);

INSERT INTO household_size_history (occupancy_id, apartment_id, household_size, children_count, effective_from, changed_by_user_id, changed_by_role, reason, status, created_at)
VALUES (@occ_204, @apt_204, 4, 1, '2026-01-01', @user_204, 'RESIDENT', 'MOVE_IN', 'ACTIVE', NOW());

-- Flat 302 (1BHK, 2 people, Silent Toilet Trickle 18.7 kL)
INSERT INTO apartments (building_id, room_number, floor_number, unit_type, apartment_name, created_at)
VALUES (@bldg_id, '302', 3, '1BHK', 'Flat 302', NOW())
ON DUPLICATE KEY UPDATE apartment_name = 'Flat 302';
SET @apt_302 = (SELECT apartment_id FROM apartments WHERE building_id = @bldg_id AND room_number = '302' LIMIT 1);

INSERT INTO users (email, name, password, role, status, created_at)
VALUES ('res302@example.com', 'Karthik Raja', '$2a$10$9Vc.t17Xi2H2OUKuFN.6O.rqQVE5NDwwa4s.2cVKOilRHu5qTC5Oq', 'RESIDENT', 'ACTIVE', NOW())
ON DUPLICATE KEY UPDATE name = 'Karthik Raja';
SET @user_302 = (SELECT user_id FROM users WHERE email = 'res302@example.com' LIMIT 1);

INSERT INTO residents (user_id, building_id, name, email, phone, apartment_number, invitation_status, account_status, created_at)
VALUES (@user_302, @bldg_id, 'Karthik Raja', 'res302@example.com', '9876500302', '302', 'ACCEPTED', 'ACTIVE', NOW())
ON DUPLICATE KEY UPDATE name = 'Karthik Raja';
SET @res_302 = (SELECT resident_id FROM residents WHERE email = 'res302@example.com' LIMIT 1);

INSERT INTO water_meters (building_id, apartment_id, resident_id, meter_number, installation_date, status)
VALUES (@bldg_id, @apt_302, @res_302, 'WM-SUK-302', '2025-12-01', 'ACTIVE')
ON DUPLICATE KEY UPDATE status = 'ACTIVE';
SET @meter_302 = (SELECT meter_id FROM water_meters WHERE meter_number = 'WM-SUK-302' LIMIT 1);

INSERT INTO apartment_occupancies (apartment_id, resident_id, start_date, status, household_size, children_count, household_size_source, household_size_confirmed, created_at)
VALUES (@apt_302, @res_302, '2026-01-01', 'ACTIVE', 2, 0, 'RESIDENT_DECLARED', TRUE, NOW())
ON DUPLICATE KEY UPDATE household_size = 2, household_size_confirmed = TRUE;
SET @occ_302 = (SELECT occupancy_id FROM apartment_occupancies WHERE apartment_id = @apt_302 AND status = 'ACTIVE' LIMIT 1);

INSERT INTO household_size_history (occupancy_id, apartment_id, household_size, children_count, effective_from, changed_by_user_id, changed_by_role, reason, status, created_at)
VALUES (@occ_302, @apt_302, 2, 0, '2026-01-01', @user_302, 'RESIDENT', 'MOVE_IN', 'ACTIVE', NOW());

-- Flat 405 (3BHK, 3 people, Away Period 18 days -> drops to 1.2 kL -> NORMAL)
INSERT INTO apartments (building_id, room_number, floor_number, unit_type, apartment_name, created_at)
VALUES (@bldg_id, '405', 4, '3BHK', 'Flat 405', NOW())
ON DUPLICATE KEY UPDATE apartment_name = 'Flat 405';
SET @apt_405 = (SELECT apartment_id FROM apartments WHERE building_id = @bldg_id AND room_number = '405' LIMIT 1);

INSERT INTO users (email, name, password, role, status, created_at)
VALUES ('res405@example.com', 'Deepa Verma', '$2a$10$9Vc.t17Xi2H2OUKuFN.6O.rqQVE5NDwwa4s.2cVKOilRHu5qTC5Oq', 'RESIDENT', 'ACTIVE', NOW())
ON DUPLICATE KEY UPDATE name = 'Deepa Verma';
SET @user_405 = (SELECT user_id FROM users WHERE email = 'res405@example.com' LIMIT 1);

INSERT INTO residents (user_id, building_id, name, email, phone, apartment_number, invitation_status, account_status, created_at)
VALUES (@user_405, @bldg_id, 'Deepa Verma', 'res405@example.com', '9876500405', '405', 'ACCEPTED', 'ACTIVE', NOW())
ON DUPLICATE KEY UPDATE name = 'Deepa Verma';
SET @res_405 = (SELECT resident_id FROM residents WHERE email = 'res405@example.com' LIMIT 1);

INSERT INTO water_meters (building_id, apartment_id, resident_id, meter_number, installation_date, status)
VALUES (@bldg_id, @apt_405, @res_405, 'WM-SUK-405', '2025-12-01', 'ACTIVE')
ON DUPLICATE KEY UPDATE status = 'ACTIVE';
SET @meter_405 = (SELECT meter_id FROM water_meters WHERE meter_number = 'WM-SUK-405' LIMIT 1);

INSERT INTO apartment_occupancies (apartment_id, resident_id, start_date, status, household_size, children_count, household_size_source, household_size_confirmed, created_at)
VALUES (@apt_405, @res_405, '2026-01-01', 'ACTIVE', 3, 0, 'RESIDENT_DECLARED', TRUE, NOW())
ON DUPLICATE KEY UPDATE household_size = 3, household_size_confirmed = TRUE;
SET @occ_405 = (SELECT occupancy_id FROM apartment_occupancies WHERE apartment_id = @apt_405 AND status = 'ACTIVE' LIMIT 1);

INSERT INTO household_size_history (occupancy_id, apartment_id, household_size, children_count, effective_from, changed_by_user_id, changed_by_role, reason, status, created_at)
VALUES (@occ_405, @apt_405, 3, 0, '2026-01-01', @user_405, 'RESIDENT', 'MOVE_IN', 'ACTIVE', NOW());

-- Log 18 Away days in October cycle
INSERT INTO away_periods (occupancy_id, apartment_id, start_date, end_date, note, created_at)
VALUES (@occ_405, @apt_405, '2026-10-05', '2026-10-22', 'Family Vacation to Shimla', NOW());

-- Flat 501 (2BHK, 3 people, Creeping leak: Month -2: 12.2 kL -> Month -1: 17.4 kL -> Current: 23.1 kL)
INSERT INTO apartments (building_id, room_number, floor_number, unit_type, apartment_name, created_at)
VALUES (@bldg_id, '501', 5, '2BHK', 'Flat 501', NOW())
ON DUPLICATE KEY UPDATE apartment_name = 'Flat 501';
SET @apt_501 = (SELECT apartment_id FROM apartments WHERE building_id = @bldg_id AND room_number = '501' LIMIT 1);

INSERT INTO users (email, name, password, role, status, created_at)
VALUES ('res501@example.com', 'Ramesh Patel', '$2a$10$9Vc.t17Xi2H2OUKuFN.6O.rqQVE5NDwwa4s.2cVKOilRHu5qTC5Oq', 'RESIDENT', 'ACTIVE', NOW())
ON DUPLICATE KEY UPDATE name = 'Ramesh Patel';
SET @user_501 = (SELECT user_id FROM users WHERE email = 'res501@example.com' LIMIT 1);

INSERT INTO residents (user_id, building_id, name, email, phone, apartment_number, invitation_status, account_status, created_at)
VALUES (@user_501, @bldg_id, 'Ramesh Patel', 'res501@example.com', '9876500501', '501', 'ACCEPTED', 'ACTIVE', NOW())
ON DUPLICATE KEY UPDATE name = 'Ramesh Patel';
SET @res_501 = (SELECT resident_id FROM residents WHERE email = 'res501@example.com' LIMIT 1);

INSERT INTO water_meters (building_id, apartment_id, resident_id, meter_number, installation_date, status)
VALUES (@bldg_id, @apt_501, @res_501, 'WM-SUK-501', '2025-12-01', 'ACTIVE')
ON DUPLICATE KEY UPDATE status = 'ACTIVE';
SET @meter_501 = (SELECT meter_id FROM water_meters WHERE meter_number = 'WM-SUK-501' LIMIT 1);

INSERT INTO apartment_occupancies (apartment_id, resident_id, start_date, status, household_size, children_count, household_size_source, household_size_confirmed, created_at)
VALUES (@apt_501, @res_501, '2026-01-01', 'ACTIVE', 3, 1, 'RESIDENT_DECLARED', TRUE, NOW())
ON DUPLICATE KEY UPDATE household_size = 3, household_size_confirmed = TRUE;
SET @occ_501 = (SELECT occupancy_id FROM apartment_occupancies WHERE apartment_id = @apt_501 AND status = 'ACTIVE' LIMIT 1);

INSERT INTO household_size_history (occupancy_id, apartment_id, household_size, children_count, effective_from, changed_by_user_id, changed_by_role, reason, status, created_at)
VALUES (@occ_501, @apt_501, 3, 1, '2026-01-01', @user_501, 'RESIDENT', 'MOVE_IN', 'ACTIVE', NOW());

-- 6. Insert Historical Water Usage Readings (Cycles May - Sep)
-- Flat 101 historical (12.5, 13.1, 12.8, 13.4, 12.9)
INSERT INTO water_usage (meter_id, billing_cycle_id, month, year, previous_reading, current_reading, units_consumed, anomaly_status, anomaly_score, expected_min, expected_max, recorded_at) VALUES
(@meter_101, @cycle_may, 5, 2026, 100.0, 112.5, 12.5, 'NORMAL', 5.0, 11.5, 14.5, '2026-05-31 18:00:00'),
(@meter_101, @cycle_jun, 6, 2026, 112.5, 125.6, 13.1, 'NORMAL', 6.0, 11.5, 14.5, '2026-06-30 18:00:00'),
(@meter_101, @cycle_jul, 7, 2026, 125.6, 138.4, 12.8, 'NORMAL', 5.0, 11.5, 14.5, '2026-07-31 18:00:00'),
(@meter_101, @cycle_aug, 8, 2026, 138.4, 151.8, 13.4, 'NORMAL', 7.0, 11.5, 15.0, '2026-08-31 18:00:00'),
(@meter_101, @cycle_sep, 9, 2026, 151.8, 164.7, 12.9, 'NORMAL', 5.0, 11.5, 14.8, '2026-09-30 18:00:00');

-- Flat 204 historical (~16 kL)
INSERT INTO water_usage (meter_id, billing_cycle_id, month, year, previous_reading, current_reading, units_consumed, anomaly_status, anomaly_score, expected_min, expected_max, recorded_at) VALUES
(@meter_204, @cycle_may, 5, 2026, 200.0, 216.0, 16.0, 'NORMAL', 8.0, 14.5, 18.0, '2026-05-31 18:00:00'),
(@meter_204, @cycle_jun, 6, 2026, 216.0, 232.5, 16.5, 'NORMAL', 9.0, 14.5, 18.0, '2026-06-30 18:00:00'),
(@meter_204, @cycle_jul, 7, 2026, 232.5, 248.3, 15.8, 'NORMAL', 8.0, 14.5, 18.0, '2026-07-31 18:00:00'),
(@meter_204, @cycle_aug, 8, 2026, 248.3, 264.7, 16.4, 'NORMAL', 9.0, 14.5, 18.2, '2026-08-31 18:00:00'),
(@meter_204, @cycle_sep, 9, 2026, 264.7, 281.0, 16.3, 'NORMAL', 9.0, 14.5, 18.0, '2026-09-30 18:00:00');

-- Flat 302 historical (~9 kL)
INSERT INTO water_usage (meter_id, billing_cycle_id, month, year, previous_reading, current_reading, units_consumed, anomaly_status, anomaly_score, expected_min, expected_max, recorded_at) VALUES
(@meter_302, @cycle_may, 5, 2026, 50.0, 59.0, 9.0, 'NORMAL', 4.0, 8.0, 10.5, '2026-05-31 18:00:00'),
(@meter_302, @cycle_jun, 6, 2026, 59.0, 68.2, 9.2, 'NORMAL', 5.0, 8.0, 10.5, '2026-06-30 18:00:00'),
(@meter_302, @cycle_jul, 7, 2026, 68.2, 77.1, 8.9, 'NORMAL', 4.0, 8.0, 10.5, '2026-07-31 18:00:00'),
(@meter_302, @cycle_aug, 8, 2026, 77.1, 86.2, 9.1, 'NORMAL', 5.0, 8.0, 10.5, '2026-08-31 18:00:00'),
(@meter_302, @cycle_sep, 9, 2026, 86.2, 95.0, 8.8, 'NORMAL', 4.0, 8.0, 10.5, '2026-09-30 18:00:00');

-- Flat 405 historical (~14 kL)
INSERT INTO water_usage (meter_id, billing_cycle_id, month, year, previous_reading, current_reading, units_consumed, anomaly_status, anomaly_score, expected_min, expected_max, recorded_at) VALUES
(@meter_405, @cycle_may, 5, 2026, 120.0, 134.2, 14.2, 'NORMAL', 6.0, 12.5, 16.0, '2026-05-31 18:00:00'),
(@meter_405, @cycle_jun, 6, 2026, 134.2, 148.0, 13.8, 'NORMAL', 5.0, 12.5, 16.0, '2026-06-30 18:00:00'),
(@meter_405, @cycle_jul, 7, 2026, 148.0, 162.3, 14.3, 'NORMAL', 6.0, 12.5, 16.0, '2026-07-31 18:00:00'),
(@meter_405, @cycle_aug, 8, 2026, 162.3, 176.1, 13.8, 'NORMAL', 5.0, 12.5, 16.0, '2026-08-31 18:00:00'),
(@meter_405, @cycle_sep, 9, 2026, 176.1, 190.2, 14.1, 'NORMAL', 6.0, 12.5, 16.0, '2026-09-30 18:00:00');

-- Flat 501 historical (Month -2: 12.2 kL -> Month -1: 17.4 kL)
INSERT INTO water_usage (meter_id, billing_cycle_id, month, year, previous_reading, current_reading, units_consumed, anomaly_status, anomaly_score, expected_min, expected_max, recorded_at) VALUES
(@meter_501, @cycle_may, 5, 2026, 80.0, 92.1, 12.1, 'NORMAL', 5.0, 11.0, 14.0, '2026-05-31 18:00:00'),
(@meter_501, @cycle_jun, 6, 2026, 92.1, 104.3, 12.2, 'NORMAL', 5.0, 11.0, 14.0, '2026-06-30 18:00:00'),
(@meter_501, @cycle_jul, 7, 2026, 104.3, 116.5, 12.2, 'NORMAL', 5.0, 11.0, 14.0, '2026-07-31 18:00:00'),
(@meter_501, @cycle_aug, 8, 2026, 116.5, 128.7, 12.2, 'NORMAL', 5.0, 11.0, 14.0, '2026-08-31 18:00:00');

-- Month -1 (Sep 2026) for Flat 501: creeping spike begins (17.4 kL)
INSERT INTO water_usage (meter_id, billing_cycle_id, month, year, previous_reading, current_reading, units_consumed, anomaly_status, anomaly_score, anomaly_pattern, anomaly_reason, expected_min, expected_max, recorded_at, scored_at)
VALUES (
    @meter_501,
    @cycle_sep,
    9,
    2026,
    128.7,
    146.1,
    17.4,
    'POTENTIAL_LEAK',
    62.0,
    'UNUSUAL_INCREASE',
    'Usage is 1.4x past 3-month baseline (12.2 kL). Early abnormal upward trajectory detected.',
    11.0,
    14.0,
    '2026-09-30 18:00:00',
    NOW()
);
SET @usage_501_sep = LAST_INSERT_ID();

-- Create open leak incident for Flat 501 starting in Sep
INSERT INTO leak_incidents (apartment_id, first_usage_id, last_usage_id, peak_risk_score, status, opened_at)
VALUES (@apt_501, @usage_501_sep, @usage_501_sep, 62.0, 'OPEN', '2026-09-30 18:00:00');
SET @incident_501 = LAST_INSERT_ID();
UPDATE water_usage SET incident_id = @incident_501 WHERE usage_id = @usage_501_sep;

-- 7. Insert Current Cycle (October 2026) Usage & ML Scored Anomalies
-- Flat 101: 13.2 kL (NORMAL)
INSERT INTO water_usage (
    meter_id, billing_cycle_id, month, year, previous_reading, current_reading, units_consumed,
    anomaly_status, anomaly_score, anomaly_confidence, anomaly_reason, expected_min, expected_max, model_version, recorded_at, scored_at
) VALUES (
    @meter_101, @cycle_oct, 10, 2026, 164.7, 177.9, 13.2,
    'NORMAL', 5.0, '0.98', 'Normal household consumption within predicted bounds.', 11.5, 15.0, '1.0.0-lgb', NOW(), NOW()
);

-- Flat 204: 48.5 kL (POTENTIAL_LEAK, SUDDEN_SPIKE)
INSERT INTO water_usage (
    meter_id, billing_cycle_id, month, year, previous_reading, current_reading, units_consumed,
    anomaly_status, anomaly_score, anomaly_confidence, anomaly_pattern, anomaly_reason, expected_min, expected_max, model_version, recorded_at, scored_at
) VALUES (
    @meter_204, @cycle_oct, 10, 2026, 281.0, 329.5, 48.5,
    'POTENTIAL_LEAK', 94.0, '0.95', 'SUDDEN_SPIKE',
    'Consumption is 3.0x your 6-month average (16.2 kL) and 2.4x the building peer median (20.2 kL). Sudden catastrophic spike detected.',
    14.5, 18.0, '1.0.0-lgb', NOW(), NOW()
);
SET @usage_204_oct = LAST_INSERT_ID();

INSERT INTO leak_incidents (apartment_id, first_usage_id, last_usage_id, peak_risk_score, status, opened_at)
VALUES (@apt_204, @usage_204_oct, @usage_204_oct, 94.0, 'OPEN', NOW());
SET @incident_204 = LAST_INSERT_ID();
UPDATE water_usage SET incident_id = @incident_204 WHERE usage_id = @usage_204_oct;

-- Flat 302: 18.7 kL (POTENTIAL_LEAK, CONTINUOUS_TRICKLE)
INSERT INTO water_usage (
    meter_id, billing_cycle_id, month, year, previous_reading, current_reading, units_consumed,
    anomaly_status, anomaly_score, anomaly_confidence, anomaly_pattern, anomaly_reason, expected_min, expected_max, model_version, recorded_at, scored_at
) VALUES (
    @meter_302, @cycle_oct, 10, 2026, 95.0, 113.7, 18.7,
    'POTENTIAL_LEAK', 76.0, '0.91', 'CONTINUOUS_TRICKLE',
    'Consumption is 2.1x 6-month average (9.0 kL). Continuous trickle pattern detected (potential running toilet flapper or cistern overflow).',
    8.0, 10.5, '1.0.0-lgb', NOW(), NOW()
);
SET @usage_302_oct = LAST_INSERT_ID();

INSERT INTO leak_incidents (apartment_id, first_usage_id, last_usage_id, peak_risk_score, status, opened_at)
VALUES (@apt_302, @usage_302_oct, @usage_302_oct, 76.0, 'OPEN', NOW());
SET @incident_302 = LAST_INSERT_ID();
UPDATE water_usage SET incident_id = @incident_302 WHERE usage_id = @usage_302_oct;

-- Flat 405: 1.2 kL with 18 Away Days (NORMAL)
INSERT INTO water_usage (
    meter_id, billing_cycle_id, month, year, previous_reading, current_reading, units_consumed,
    anomaly_status, anomaly_score, anomaly_confidence, anomaly_reason, expected_min, expected_max, model_version, recorded_at, scored_at
) VALUES (
    @meter_405, @cycle_oct, 10, 2026, 190.2, 191.4, 1.2,
    'NORMAL', 4.0, '0.99', 'Low usage expected due to 18 registered away days. Baseline adjusted accordingly.',
    0.5, 5.0, '1.0.0-lgb', NOW(), NOW()
);

-- Flat 501: 23.1 kL (POTENTIAL_LEAK, MULTI_CYCLE_CREEP, linked to existing incident)
INSERT INTO water_usage (
    meter_id, billing_cycle_id, month, year, previous_reading, current_reading, units_consumed,
    anomaly_status, anomaly_score, anomaly_confidence, anomaly_pattern, anomaly_reason, expected_min, expected_max, incident_id, model_version, recorded_at, scored_at
) VALUES (
    @meter_501, @cycle_oct, 10, 2026, 146.1, 169.2, 23.1,
    'POTENTIAL_LEAK', 84.0, '0.92', 'MULTI_CYCLE_CREEP',
    'Creeping consumption increase detected over 3 consecutive cycles: 12.2 kL -> 17.4 kL -> 23.1 kL.',
    11.0, 14.0, @incident_501, '1.0.0-lgb', NOW(), NOW()
);
SET @usage_501_oct = LAST_INSERT_ID();
UPDATE leak_incidents SET last_usage_id = @usage_501_oct, peak_risk_score = 84.0 WHERE id = @incident_501;

SET FOREIGN_KEY_CHECKS = 1;

-- Verification
SELECT 'Demo Seed Completed' AS status, COUNT(*) AS sukimiya_flats FROM apartments WHERE building_id = @bldg_id;
