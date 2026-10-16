-- Seed Leak Data for Michael Scott (michael.resident@gmail.com)
-- Flat Apt 4B, Building 1, Meter 1 (WM-SKY-4B)

-- Clean up any prior leak feedback or duplicate usage records for meter 1
DELETE FROM leak_feedback WHERE apartment_id = 1;
DELETE FROM water_usage WHERE meter_id = 1 AND month IN (8, 9, 10) AND year = 2026;

-- Update existing July record (usage_id = 1) to normal baseline
UPDATE water_usage 
SET units_consumed = 14.5,
    previous_reading = 100.0,
    current_reading = 114.5,
    anomaly_status = 'NORMAL',
    anomaly_score = 5.0,
    anomaly_confidence = 'MEDIUM',
    anomaly_pattern = 'NORMAL',
    expected_min = 12.0,
    expected_max = 16.0,
    anomaly_reason = 'Consumption appears normal.'
WHERE usage_id = 1;

-- Insert August 2026 baseline
INSERT INTO water_usage (meter_id, month, year, previous_reading, current_reading, units_consumed, recorded_at, anomaly_status, anomaly_score, anomaly_confidence, anomaly_pattern, expected_min, expected_max, anomaly_reason, model_version, scored_at)
VALUES (1, 8, 2026, 114.5, 129.5, 15.0, '2026-08-31 10:00:00', 'NORMAL', 6.0, 'MEDIUM', 'NORMAL', 12.5, 16.5, 'Usage is within normal statistical thresholds based on past cycles.', 'v1.1.0', '2026-08-31 10:00:00');

-- Insert September 2026 baseline
INSERT INTO water_usage (meter_id, month, year, previous_reading, current_reading, units_consumed, recorded_at, anomaly_status, anomaly_score, anomaly_confidence, anomaly_pattern, expected_min, expected_max, anomaly_reason, model_version, scored_at)
VALUES (1, 9, 2026, 129.5, 144.3, 14.8, '2026-09-30 10:00:00', 'NORMAL', 5.0, 'MEDIUM', 'NORMAL', 12.5, 16.5, 'Usage is within normal statistical thresholds based on past cycles.', 'v1.1.0', '2026-09-30 10:00:00');

-- Insert October 2026 Leak Record (SUDDEN_SPIKE: 48.5 kL)
INSERT INTO water_usage (meter_id, month, year, previous_reading, current_reading, units_consumed, recorded_at, billing_cycle_id, anomaly_status, anomaly_score, anomaly_confidence, anomaly_pattern, expected_min, expected_max, anomaly_reason, model_version, scored_at)
VALUES (1, 10, 2026, 144.3, 192.8, 48.5, NOW(), 1, 'POTENTIAL_LEAK', 96.0, 'HIGH', 'SUDDEN_SPIKE', 13.0, 16.8, 'Usage (48.5 kL) significantly exceeds historical baseline threshold (16.8 kL). Continuous toilet flapper leak suspected.', 'v1.1.0', NOW());

SET @new_usage_id = LAST_INSERT_ID();

-- Create OPEN Leak Incident for Apartment 1
INSERT INTO leak_incidents (apartment_id, first_usage_id, last_usage_id, status, peak_risk_score, opened_at)
VALUES (1, @new_usage_id, @new_usage_id, 'OPEN', 96.0, NOW());

SET @new_incident_id = LAST_INSERT_ID();

-- Link incident back to usage
UPDATE water_usage SET incident_id = @new_incident_id WHERE usage_id = @new_usage_id;
