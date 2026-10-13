-- V5: Add Billing Cycle Container Relationships, 3-Tier Tariff, and Unique Constraints

-- 1. Update billing_cycles table
ALTER TABLE billing_cycles ADD COLUMN IF NOT EXISTS billing_month DATE;
UPDATE billing_cycles SET billing_month = DATE_TRUNC('month', start_date) WHERE billing_month IS NULL;
ALTER TABLE billing_cycles ALTER COLUMN billing_month SET NOT NULL;

-- Unique constraint: One building + One calendar month = One billing cycle
ALTER TABLE billing_cycles DROP CONSTRAINT IF EXISTS idx_unique_building_billing_month;
ALTER TABLE billing_cycles ADD CONSTRAINT idx_unique_building_billing_month UNIQUE (building_id, billing_month);

-- 2. Update water_usage table
ALTER TABLE water_usage ADD COLUMN IF NOT EXISTS billing_cycle_id BIGINT REFERENCES billing_cycles(id);
ALTER TABLE water_usage ADD COLUMN IF NOT EXISTS anomaly_status VARCHAR(50) DEFAULT 'NORMAL';

-- 3. Update water_purchases table
ALTER TABLE water_purchases ADD COLUMN IF NOT EXISTS billing_cycle_id BIGINT REFERENCES billing_cycles(id);

-- 4. Update tariff_plans table for 3-Tier Tariff Architecture
ALTER TABLE tariff_plans ADD COLUMN IF NOT EXISTS tier1_limit NUMERIC(10,2) DEFAULT 20.00;
ALTER TABLE tariff_plans ADD COLUMN IF NOT EXISTS tier1_rate NUMERIC(10,2) DEFAULT 2.50;
ALTER TABLE tariff_plans ADD COLUMN IF NOT EXISTS tier2_limit NUMERIC(10,2) DEFAULT 40.00;
ALTER TABLE tariff_plans ADD COLUMN IF NOT EXISTS tier2_rate NUMERIC(10,2) DEFAULT 4.00;
ALTER TABLE tariff_plans ADD COLUMN IF NOT EXISTS tier3_rate NUMERIC(10,2) DEFAULT 6.00;

-- 5. Update invoices and bills tables with 3-Tier Itemized Breakdown Snapshot
ALTER TABLE invoices ADD COLUMN IF NOT EXISTS tier1_units NUMERIC(10,2) DEFAULT 0.00;
ALTER TABLE invoices ADD COLUMN IF NOT EXISTS tier1_rate NUMERIC(10,2) DEFAULT 0.00;
ALTER TABLE invoices ADD COLUMN IF NOT EXISTS tier1_amount NUMERIC(10,2) DEFAULT 0.00;
ALTER TABLE invoices ADD COLUMN IF NOT EXISTS tier2_units NUMERIC(10,2) DEFAULT 0.00;
ALTER TABLE invoices ADD COLUMN IF NOT EXISTS tier2_rate NUMERIC(10,2) DEFAULT 0.00;
ALTER TABLE invoices ADD COLUMN IF NOT EXISTS tier2_amount NUMERIC(10,2) DEFAULT 0.00;
ALTER TABLE invoices ADD COLUMN IF NOT EXISTS tier3_units NUMERIC(10,2) DEFAULT 0.00;
ALTER TABLE invoices ADD COLUMN IF NOT EXISTS tier3_rate NUMERIC(10,2) DEFAULT 0.00;
ALTER TABLE invoices ADD COLUMN IF NOT EXISTS tier3_amount NUMERIC(10,2) DEFAULT 0.00;
ALTER TABLE bills ADD COLUMN IF NOT EXISTS billing_cycle_id BIGINT REFERENCES billing_cycles(id);
ALTER TABLE bills ADD COLUMN IF NOT EXISTS tier1_units NUMERIC(10,2) DEFAULT 0.00;
ALTER TABLE bills ADD COLUMN IF NOT EXISTS tier1_rate NUMERIC(10,2) DEFAULT 0.00;
ALTER TABLE bills ADD COLUMN IF NOT EXISTS tier1_amount NUMERIC(10,2) DEFAULT 0.00;
ALTER TABLE bills ADD COLUMN IF NOT EXISTS tier2_units NUMERIC(10,2) DEFAULT 0.00;
ALTER TABLE bills ADD COLUMN IF NOT EXISTS tier2_rate NUMERIC(10,2) DEFAULT 0.00;
ALTER TABLE bills ADD COLUMN IF NOT EXISTS tier2_amount NUMERIC(10,2) DEFAULT 0.00;
ALTER TABLE bills ADD COLUMN IF NOT EXISTS tier3_units NUMERIC(10,2) DEFAULT 0.00;
ALTER TABLE bills ADD COLUMN IF NOT EXISTS tier3_rate NUMERIC(10,2) DEFAULT 0.00;
ALTER TABLE bills ADD COLUMN IF NOT EXISTS tier3_amount NUMERIC(10,2) DEFAULT 0.00;
