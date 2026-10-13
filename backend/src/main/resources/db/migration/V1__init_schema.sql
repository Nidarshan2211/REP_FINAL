-- Smart Water Billing & Management System Schema SQL
-- Compatible with PostgreSQL

CREATE TABLE IF NOT EXISTS users (
    user_id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(150) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL,
    status VARCHAR(50) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS building_owners (
    owner_id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
    name VARCHAR(100) NOT NULL,
    phone VARCHAR(20) NOT NULL,
    approval_status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS buildings (
    building_id BIGSERIAL PRIMARY KEY,
    owner_id BIGINT NOT NULL REFERENCES building_owners(owner_id) ON DELETE CASCADE,
    building_name VARCHAR(150) NOT NULL,
    building_number VARCHAR(50) NOT NULL,
    total_flats INT NOT NULL,
    location VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS residents (
    resident_id BIGSERIAL PRIMARY KEY,
    user_id BIGINT REFERENCES users(user_id) ON DELETE SET NULL,
    building_id BIGINT NOT NULL REFERENCES buildings(building_id) ON DELETE CASCADE,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(150) NOT NULL,
    phone VARCHAR(20) NOT NULL,
    apartment_number VARCHAR(20) NOT NULL,
    invitation_token VARCHAR(255),
    invitation_status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    account_status VARCHAR(50) NOT NULL DEFAULT 'INACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS water_meters (
    meter_id BIGSERIAL PRIMARY KEY,
    building_id BIGINT NOT NULL REFERENCES buildings(building_id) ON DELETE CASCADE,
    resident_id BIGINT REFERENCES residents(resident_id) ON DELETE SET NULL,
    meter_number VARCHAR(50) UNIQUE NOT NULL,
    installation_date DATE NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE'
);

CREATE TABLE IF NOT EXISTS water_usage (
    usage_id BIGSERIAL PRIMARY KEY,
    meter_id BIGINT NOT NULL REFERENCES water_meters(meter_id) ON DELETE CASCADE,
    month INT NOT NULL,
    year INT NOT NULL,
    previous_reading DOUBLE PRECISION NOT NULL,
    current_reading DOUBLE PRECISION NOT NULL,
    units_consumed DOUBLE PRECISION NOT NULL,
    recorded_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS tariff (
    tariff_id BIGSERIAL PRIMARY KEY,
    minimum_units INT NOT NULL,
    maximum_units INT NOT NULL,
    price_per_unit DOUBLE PRECISION NOT NULL,
    fixed_charge DOUBLE PRECISION NOT NULL
);

CREATE TABLE IF NOT EXISTS bills (
    bill_id BIGSERIAL PRIMARY KEY,
    resident_id BIGINT NOT NULL REFERENCES residents(resident_id) ON DELETE CASCADE,
    usage_id BIGINT NOT NULL REFERENCES water_usage(usage_id) ON DELETE CASCADE,
    bill_month INT NOT NULL,
    bill_year INT NOT NULL,
    units DOUBLE PRECISION NOT NULL,
    amount DOUBLE PRECISION NOT NULL,
    due_date DATE NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'UNPAID',
    generated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS payments (
    payment_id BIGSERIAL PRIMARY KEY,
    bill_id BIGINT NOT NULL REFERENCES bills(bill_id) ON DELETE CASCADE,
    amount DOUBLE PRECISION NOT NULL,
    payment_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    payment_method VARCHAR(50) NOT NULL,
    transaction_reference VARCHAR(100) NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'SUCCESS'
);

CREATE TABLE IF NOT EXISTS notifications (
    notification_id BIGSERIAL PRIMARY KEY,
    resident_id BIGINT NOT NULL REFERENCES residents(resident_id) ON DELETE CASCADE,
    title VARCHAR(150) NOT NULL,
    message TEXT NOT NULL,
    is_read BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
