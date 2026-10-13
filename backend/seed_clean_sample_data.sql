-- CLEAN & RESET DATABASE TABLES WITH FRESH SAMPLE DATA

-- 1. TRUNCATE ALL EXISTING TABLES
TRUNCATE TABLE 
    payments, 
    invoices, 
    bills, 
    notifications, 
    water_usage, 
    water_purchases, 
    tariff_plans, 
    tariff, 
    billing_cycles, 
    water_meters, 
    residents, 
    buildings, 
    building_owners, 
    users 
RESTART IDENTITY CASCADE;

-- 2. SEED USERS & ROLES
-- Default Password for all accounts: Admin@123456 (or password123)
INSERT INTO users (user_id, name, email, password, role, status, created_at) VALUES
(1, 'System Super Admin', 'admin@smartwater.com', '$2a$10$eE0m9Zk9P.tL9K0xR2P0.uM9wJvJ7J1rP3W8xL6mK2nQ9tV2wM1.S', 'SUPER_ADMIN', 'APPROVED', NOW()),
(2, 'Robert Sterling', 'owner.robert@gmail.com', '$2a$10$eE0m9Zk9P.tL9K0xR2P0.uM9wJvJ7J1rP3W8xL6mK2nQ9tV2wM1.S', 'BUILDING_OWNER', 'APPROVED', NOW()),
(3, 'Michael Scott', 'michael.resident@gmail.com', '$2a$10$eE0m9Zk9P.tL9K0xR2P0.uM9wJvJ7J1rP3W8xL6mK2nQ9tV2wM1.S', 'RESIDENT', 'ACTIVE', NOW()),
(4, 'Jim Halpert', 'jim.halpert@gmail.com', '$2a$10$eE0m9Zk9P.tL9K0xR2P0.uM9wJvJ7J1rP3W8xL6mK2nQ9tV2wM1.S', 'RESIDENT', 'ACTIVE', NOW()),
(5, 'Ravi Kumar', 'ravi.kumar@gmail.com', '$2a$10$eE0m9Zk9P.tL9K0xR2P0.uM9wJvJ7J1rP3W8xL6mK2nQ9tV2wM1.S', 'RESIDENT', 'ACTIVE', NOW()),
(6, 'Shubha R', 'shubha.resident@gmail.com', '$2a$10$eE0m9Zk9P.tL9K0xR2P0.uM9wJvJ7J1rP3W8xL6mK2nQ9tV2wM1.S', 'RESIDENT', 'ACTIVE', NOW());

-- 3. SEED BUILDING OWNER
INSERT INTO building_owners (owner_id, user_id, name, phone, approval_status, created_at) VALUES
(1, 2, 'Robert Sterling', '+1 555-0192', 'APPROVED', NOW());

-- 4. SEED BUILDINGS
INSERT INTO buildings (building_id, owner_id, building_name, building_number, total_flats, location, created_at) VALUES
(1, 1, 'Skyline Luxury Towers', 'B-101', 24, '742 Evergreen Terrace, Sector 4', NOW()),
(2, 1, 'Emerald Heights', 'EH-04', 16, '120 Ocean Drive, Block B', NOW());

-- 5. SEED RESIDENTS
INSERT INTO residents (resident_id, user_id, building_id, name, email, phone, apartment_number, invitation_token, invitation_status, account_status, created_at) VALUES
(1, 3, 1, 'Michael Scott', 'michael.resident@gmail.com', '+1 555-9012', 'Apt 4B', 'INV-TOKEN-001', 'ACCEPTED', 'ACTIVE', NOW()),
(2, 4, 1, 'Jim Halpert', 'jim.halpert@gmail.com', '+1 555-4321', 'Apt 3A', 'INV-TOKEN-002', 'ACCEPTED', 'ACTIVE', NOW()),
(3, 5, 1, 'Ravi Kumar', 'ravi.kumar@gmail.com', '+1 555-8821', 'Apt 2B', 'INV-TOKEN-003', 'ACCEPTED', 'ACTIVE', NOW()),
(4, 6, 2, 'Shubha R', 'shubha.resident@gmail.com', '+1 555-7711', 'Apt 45', 'INV-TOKEN-004', 'ACCEPTED', 'ACTIVE', NOW());

-- 6. SEED WATER METERS
INSERT INTO water_meters (meter_id, building_id, resident_id, meter_number, installation_date, status) VALUES
(1, 1, 1, 'WM-SKY-4B', '2025-01-15', 'ACTIVE'),
(2, 1, 2, 'WM-SKY-3A', '2025-01-15', 'ACTIVE'),
(3, 1, 3, 'WM-SKY-2B', '2025-01-15', 'ACTIVE'),
(4, 2, 4, 'WM-EH-45', '2025-01-15', 'ACTIVE');

-- 7. SEED BILLING CYCLES
INSERT INTO billing_cycles (id, building_id, start_date, end_date, status) VALUES
(1, 1, '2026-08-01', '2026-08-31', 'OPEN'),
(2, 2, '2026-08-01', '2026-08-31', 'OPEN');

-- 8. SEED TARIFF PLANS
INSERT INTO tariff_plans (id, building_id, billing_cycle_id, base_limit, base_rate, extra_rate, fixed_charge, status, effective_date, created_at) VALUES
(1, 1, 1, 20.00, 2.50, 4.00, 0.00, 'ACTIVE', NOW(), NOW()),
(2, 2, 2, 20.00, 2.50, 4.00, 0.00, 'ACTIVE', NOW(), NOW());

-- 9. SEED BULK WATER PURCHASES
INSERT INTO water_purchases (id, building_id, supplier, quantity, volume, unit_cost, total_cost, purchase_date) VALUES
(1, 1, 'Municipal Water Corporation', 1000.0, 1000.0, 50.00, 50000.00, '2026-08-05'),
(2, 1, 'Water Tanker Vendor', 500.0, 500.0, 65.00, 32500.00, '2026-08-08');

-- 10. SEED WATER USAGE LOGS
INSERT INTO water_usage (usage_id, meter_id, month, year, previous_reading, current_reading, units_consumed, recorded_at) VALUES
(1, 1, 8, 2026, 120.0, 155.0, 35.0, NOW()),
(2, 2, 8, 2026, 190.0, 210.0, 20.0, NOW()),
(3, 3, 8, 2026, 100.0, 150.0, 50.0, NOW()),
(4, 4, 8, 2026, 80.0, 95.0, 15.0, NOW());

-- 11. SEED BILLS (Calculated purely using active tariff: Base 20u @ ₹2.50, Extra @ ₹4.00)
-- Michael Scott (35u): 20*2.5 + 15*4.0 = ₹110.00
-- Jim Halpert (20u): 20*2.5 = ₹50.00
-- Ravi Kumar (50u): 20*2.5 + 30*4.0 = ₹170.00
-- Shubha R (15u): 15*2.5 = ₹37.50
INSERT INTO bills (bill_id, resident_id, usage_id, bill_month, bill_year, units, amount, due_date, status, generated_at) VALUES
(1, 1, 1, 8, 2026, 35.0, 110.00, '2026-08-26', 'UNPAID', NOW()),
(2, 2, 2, 8, 2026, 20.0, 50.00, '2026-08-26', 'UNPAID', NOW()),
(3, 3, 3, 8, 2026, 50.0, 170.00, '2026-08-26', 'UNPAID', NOW()),
(4, 4, 4, 8, 2026, 15.0, 37.50, '2026-08-26', 'UNPAID', NOW());

-- 12. SEED INVOICES
INSERT INTO invoices (id, billing_cycle_id, resident_id, usage_units, base_charge, extra_charge, shared_water_charge, total_amount, due_date, status, created_at) VALUES
(1, 1, 1, 35.00, 50.00, 60.00, 0.00, 110.00, '2026-08-26', 'UNPAID', NOW()),
(2, 1, 2, 20.00, 50.00, 0.00, 0.00, 50.00, '2026-08-26', 'UNPAID', NOW()),
(3, 1, 3, 50.00, 50.00, 120.00, 0.00, 170.00, '2026-08-26', 'UNPAID', NOW()),
(4, 2, 4, 15.00, 37.50, 0.00, 0.00, 37.50, '2026-08-26', 'UNPAID', NOW());

-- 13. SEED NOTIFICATIONS
INSERT INTO notifications (notification_id, resident_id, title, message, is_read, created_at) VALUES
(1, 1, 'New Itemized Water Bill Issued', 'Your water bill for period (2026-08-01 to 2026-08-31) is ₹110.00. Usage: 35.0 kL. Due Date: 2026-08-26.', FALSE, NOW()),
(2, 2, 'New Itemized Water Bill Issued', 'Your water bill for period (2026-08-01 to 2026-08-31) is ₹50.00. Usage: 20.0 kL. Due Date: 2026-08-26.', FALSE, NOW()),
(3, 3, 'New Itemized Water Bill Issued', 'Your water bill for period (2026-08-01 to 2026-08-31) is ₹170.00. Usage: 50.0 kL. Due Date: 2026-08-26.', FALSE, NOW()),
(4, 4, 'New Itemized Water Bill Issued', 'Your water bill for period (2026-08-01 to 2026-08-31) is ₹37.50. Usage: 15.0 kL. Due Date: 2026-08-26.', FALSE, NOW());

-- 14. RESET BIGSERIAL SEQUENCES TO PREVENT CONFLICTS
SELECT setval('users_user_id_seq', 10);
SELECT setval('building_owners_owner_id_seq', 10);
SELECT setval('buildings_building_id_seq', 10);
SELECT setval('residents_resident_id_seq', 10);
SELECT setval('water_meters_meter_id_seq', 10);
SELECT setval('billing_cycles_id_seq', 10);
SELECT setval('tariff_plans_id_seq', 10);
SELECT setval('water_purchases_id_seq', 10);
SELECT setval('water_usage_usage_id_seq', 10);
SELECT setval('bills_bill_id_seq', 10);
SELECT setval('invoices_id_seq', 10);
SELECT setval('notifications_notification_id_seq', 10);
