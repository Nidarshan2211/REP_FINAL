-- Migration Script for Week 3-4 Modules: Tiered Tariff, Billing Cycles, Water Purchases, Invoices

-- 1. Tariff Plans Table (Slab Pricing)
CREATE TABLE IF NOT EXISTS tariff_plans (
    id BIGSERIAL PRIMARY KEY,
    building_id BIGINT NOT NULL REFERENCES buildings(building_id) ON DELETE CASCADE,
    base_limit DECIMAL(10,2) NOT NULL DEFAULT 10.00,
    base_rate DECIMAL(10,2) NOT NULL DEFAULT 15.00,
    extra_rate DECIMAL(10,2) NOT NULL DEFAULT 25.00,
    effective_from DATE NOT NULL DEFAULT CURRENT_DATE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 2. Billing Cycles Table
CREATE TABLE IF NOT EXISTS billing_cycles (
    id BIGSERIAL PRIMARY KEY,
    building_id BIGINT NOT NULL REFERENCES buildings(building_id) ON DELETE CASCADE,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'OPEN',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 3. Water Purchase Tracking Table
CREATE TABLE IF NOT EXISTS water_purchases (
    id BIGSERIAL PRIMARY KEY,
    building_id BIGINT NOT NULL REFERENCES buildings(building_id) ON DELETE CASCADE,
    billing_cycle_id BIGINT REFERENCES billing_cycles(id) ON DELETE SET NULL,
    supplier VARCHAR(100) NOT NULL,
    volume DECIMAL(10,2) NOT NULL,
    unit_cost DECIMAL(10,2) NOT NULL,
    total_cost DECIMAL(10,2) NOT NULL,
    purchase_date DATE NOT NULL DEFAULT CURRENT_DATE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 4. Invoices Table (Itemized Water Bill Statements)
CREATE TABLE IF NOT EXISTS invoices (
    id BIGSERIAL PRIMARY KEY,
    billing_cycle_id BIGINT REFERENCES billing_cycles(id) ON DELETE SET NULL,
    resident_id BIGINT NOT NULL REFERENCES residents(resident_id) ON DELETE CASCADE,
    usage_units DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    base_charge DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    extra_charge DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    shared_water_charge DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    total_amount DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    due_date DATE NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'UNPAID',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Synchronize BIGSERIAL Sequences
SELECT setval('tariff_plans_id_seq', COALESCE((SELECT MAX(id) FROM tariff_plans), 1));
SELECT setval('billing_cycles_id_seq', COALESCE((SELECT MAX(id) FROM billing_cycles), 1));
SELECT setval('water_purchases_id_seq', COALESCE((SELECT MAX(id) FROM water_purchases), 1));
SELECT setval('invoices_id_seq', COALESCE((SELECT MAX(id) FROM invoices), 1));
