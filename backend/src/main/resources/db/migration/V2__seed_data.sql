-- Seed Initial Data for Smart Water Billing System

-- Super Admin User (Password: Admin@123456 -> BCrypt encoded)
INSERT INTO users (user_id, name, email, password, role, status, created_at)
VALUES (1, 'System Super Admin', 'admin@smartwater.com', '$2a$10$eE0m9Zk9P.tL9K0xR2P0.uM9wJvJ7J1rP3W8xL6mK2nQ9tV2wM1.S', 'SUPER_ADMIN', 'APPROVED', CURRENT_TIMESTAMP)
ON CONFLICT (user_id) DO NOTHING;

-- Approved Building Owner
INSERT INTO users (user_id, name, email, password, role, status, created_at)
VALUES (2, 'Robert Sterling', 'owner.robert@gmail.com', '$2a$10$eE0m9Zk9P.tL9K0xR2P0.uM9wJvJ7J1rP3W8xL6mK2nQ9tV2wM1.S', 'BUILDING_OWNER', 'APPROVED', CURRENT_TIMESTAMP)
ON CONFLICT (user_id) DO NOTHING;

INSERT INTO building_owners (owner_id, user_id, name, phone, approval_status, created_at)
VALUES (1, 2, 'Robert Sterling', '+1 555-0192', 'APPROVED', CURRENT_TIMESTAMP)
ON CONFLICT (owner_id) DO NOTHING;

-- Pending Building Owner
INSERT INTO users (user_id, name, email, password, role, status, created_at)
VALUES (3, 'Sarah Jenkins', 'owner.sarah@gmail.com', '$2a$10$eE0m9Zk9P.tL9K0xR2P0.uM9wJvJ7J1rP3W8xL6mK2nQ9tV2wM1.S', 'BUILDING_OWNER', 'PENDING', CURRENT_TIMESTAMP)
ON CONFLICT (user_id) DO NOTHING;

INSERT INTO building_owners (owner_id, user_id, name, phone, approval_status, created_at)
VALUES (2, 3, 'Sarah Jenkins', '+1 555-0843', 'PENDING', CURRENT_TIMESTAMP)
ON CONFLICT (owner_id) DO NOTHING;

-- Buildings
INSERT INTO buildings (building_id, owner_id, building_name, building_number, total_flats, location, created_at)
VALUES (1, 1, 'Skyline Luxury Towers', 'B-101', 24, '742 Evergreen Terrace, Sector 4', CURRENT_TIMESTAMP),
       (2, 1, 'Emerald Heights', 'EH-04', 16, '120 Ocean Drive, Block B', CURRENT_TIMESTAMP)
ON CONFLICT (building_id) DO NOTHING;

-- Active Resident User
INSERT INTO users (user_id, name, email, password, role, status, created_at)
VALUES (4, 'Michael Scott', 'michael.resident@gmail.com', '$2a$10$eE0m9Zk9P.tL9K0xR2P0.uM9wJvJ7J1rP3W8xL6mK2nQ9tV2wM1.S', 'RESIDENT', 'ACTIVE', CURRENT_TIMESTAMP)
ON CONFLICT (user_id) DO NOTHING;

-- Residents
INSERT INTO residents (resident_id, user_id, building_id, name, email, phone, apartment_number, invitation_token, invitation_status, account_status, created_at)
VALUES (1, 4, 1, 'Michael Scott', 'michael.resident@gmail.com', '+1 555-9012', 'Apt 4B', 'INV-TOKEN-001', 'ACCEPTED', 'ACTIVE', CURRENT_TIMESTAMP),
       (2, NULL, 1, 'Jim Halpert', 'jim.halpert@gmail.com', '+1 555-4321', 'Apt 3A', 'INV-TOKEN-002', 'PENDING', 'INACTIVE', CURRENT_TIMESTAMP)
ON CONFLICT (resident_id) DO NOTHING;

-- Water Meters
INSERT INTO water_meters (meter_id, building_id, resident_id, meter_number, installation_date, status)
VALUES (1, 1, 1, 'WM-SKY-4B', '2024-01-15', 'ACTIVE'),
       (2, 1, 2, 'WM-SKY-3A', '2024-01-15', 'ACTIVE')
ON CONFLICT (meter_id) DO NOTHING;

-- Tariff Rules
INSERT INTO tariff (tariff_id, minimum_units, maximum_units, price_per_unit, fixed_charge)
VALUES (1, 0, 20, 2.50, 10.00),
       (2, 21, 50, 4.00, 15.00),
       (3, 51, 1000, 6.50, 25.00)
ON CONFLICT (tariff_id) DO NOTHING;

-- Water Usage
INSERT INTO water_usage (usage_id, meter_id, month, year, previous_reading, current_reading, units_consumed, recorded_at)
VALUES (1, 1, 7, 2026, 120.0, 155.0, 35.0, CURRENT_TIMESTAMP)
ON CONFLICT (usage_id) DO NOTHING;

-- Bills
INSERT INTO bills (bill_id, resident_id, usage_id, bill_month, bill_year, units, amount, due_date, status, generated_at)
VALUES (1, 1, 1, 7, 2026, 35.0, 110.00, '2026-08-15', 'UNPAID', CURRENT_TIMESTAMP)
ON CONFLICT (bill_id) DO NOTHING;

-- Notifications
INSERT INTO notifications (notification_id, resident_id, title, message, is_read, created_at)
VALUES (1, 1, 'New Water Bill Issued', 'Your water bill for July 2026 of $110.00 is now available. Due date: Aug 15, 2026.', FALSE, CURRENT_TIMESTAMP)
ON CONFLICT (notification_id) DO NOTHING;

-- Synchronize PostgreSQL BIGSERIAL Sequences to prevent PK collision
SELECT setval('users_user_id_seq', COALESCE((SELECT MAX(user_id) FROM users), 1));
SELECT setval('building_owners_owner_id_seq', COALESCE((SELECT MAX(owner_id) FROM building_owners), 1));
SELECT setval('buildings_building_id_seq', COALESCE((SELECT MAX(building_id) FROM buildings), 1));
SELECT setval('residents_resident_id_seq', COALESCE((SELECT MAX(resident_id) FROM residents), 1));
SELECT setval('water_meters_meter_id_seq', COALESCE((SELECT MAX(meter_id) FROM water_meters), 1));
SELECT setval('water_usage_usage_id_seq', COALESCE((SELECT MAX(usage_id) FROM water_usage), 1));
SELECT setval('tariff_tariff_id_seq', COALESCE((SELECT MAX(tariff_id) FROM tariff), 1));
SELECT setval('bills_bill_id_seq', COALESCE((SELECT MAX(bill_id) FROM bills), 1));
SELECT setval('notifications_notification_id_seq', COALESCE((SELECT MAX(notification_id) FROM notifications), 1));
