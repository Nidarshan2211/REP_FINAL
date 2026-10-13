-- V6: Dynamic Bulk Procurement Resident Tariff Engine Schema Migration

ALTER TABLE tariff_plans ADD COLUMN IF NOT EXISTS average_procurement_cost NUMERIC(10,2) DEFAULT 0.00;
ALTER TABLE tariff_plans ADD COLUMN IF NOT EXISTS markup_percentage NUMERIC(10,2) DEFAULT 20.00;
ALTER TABLE tariff_plans ADD COLUMN IF NOT EXISTS base_tariff NUMERIC(10,2) DEFAULT 0.00;
ALTER TABLE tariff_plans ADD COLUMN IF NOT EXISTS tier1_multiplier NUMERIC(10,2) DEFAULT 1.00;
ALTER TABLE tariff_plans ADD COLUMN IF NOT EXISTS tier2_multiplier NUMERIC(10,2) DEFAULT 1.25;
ALTER TABLE tariff_plans ADD COLUMN IF NOT EXISTS tier3_multiplier NUMERIC(10,2) DEFAULT 1.50;

-- Update defaults for tier rates if null
UPDATE tariff_plans SET average_procurement_cost = 0.00 WHERE average_procurement_cost IS NULL;
UPDATE tariff_plans SET markup_percentage = 20.00 WHERE markup_percentage IS NULL;
UPDATE tariff_plans SET base_tariff = 0.00 WHERE base_tariff IS NULL;
UPDATE tariff_plans SET tier1_multiplier = 1.00 WHERE tier1_multiplier IS NULL;
UPDATE tariff_plans SET tier2_multiplier = 1.25 WHERE tier2_multiplier IS NULL;
UPDATE tariff_plans SET tier3_multiplier = 1.50 WHERE tier3_multiplier IS NULL;
