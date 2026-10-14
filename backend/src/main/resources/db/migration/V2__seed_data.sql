-- Seed Initial Data for Smart Water Billing System

-- Super Admin User (Password: Admin@123456 -> BCrypt encoded)
INSERT INTO users (user_id, name, email, password, role, status, created_at)
VALUES (1, 'System Super Admin', 'admin@smartwater.com', '$2a$10$eE0m9Zk9P.tL9K0xR2P0.uM9wJvJ7J1rP3W8xL6mK2nQ9tV2wM1.S', 'SUPER_ADMIN', 'APPROVED', CURRENT_TIMESTAMP)
ON CONFLICT (user_id) DO NOTHING;

-- Base Tariff Rules for water metering
INSERT INTO tariff (tariff_id, minimum_units, maximum_units, price_per_unit, fixed_charge)
VALUES (1, 0, 20, 2.50, 10.00),
       (2, 21, 50, 4.00, 15.00),
       (3, 51, 1000, 6.50, 25.00)
ON CONFLICT (tariff_id) DO NOTHING;

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
