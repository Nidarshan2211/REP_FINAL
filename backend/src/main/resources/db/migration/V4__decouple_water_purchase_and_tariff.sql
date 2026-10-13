-- Migration V4: Decouple Water Purchase Cost and Resident Tariff

-- 1. Update Tariff Plans Table to support Lifecycle Status and BillingCycle linkage
ALTER TABLE tariff_plans 
    ADD COLUMN IF NOT EXISTS billing_cycle_id BIGINT REFERENCES billing_cycles(id) ON DELETE CASCADE,
    ADD COLUMN IF NOT EXISTS status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',
    ADD COLUMN IF NOT EXISTS effective_date DATE DEFAULT CURRENT_DATE,
    ADD COLUMN IF NOT EXISTS fixed_charge DECIMAL(10,2) NOT NULL DEFAULT 0.00;

-- 2. Create Unique Partial Index to enforce only ONE ACTIVE TariffPlan per building and billing_cycle
DROP INDEX IF EXISTS idx_unique_active_tariff_plan;
CREATE UNIQUE INDEX idx_unique_active_tariff_plan 
    ON tariff_plans (building_id, COALESCE(billing_cycle_id, 0)) 
    WHERE status = 'ACTIVE';

-- 3. Ensure Water Purchases Table has quantity alias column if missing
ALTER TABLE water_purchases 
    ADD COLUMN IF NOT EXISTS quantity DECIMAL(10,2);

UPDATE water_purchases SET quantity = volume WHERE quantity IS NULL AND volume IS NOT NULL;
