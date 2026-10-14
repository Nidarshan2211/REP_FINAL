-- V9: Apartment Inventory and Resident Occupancy Lifecycle Schema

-- 1. Create apartments table
CREATE TABLE IF NOT EXISTS apartments (
    apartment_id BIGSERIAL PRIMARY KEY,
    building_id BIGINT NOT NULL REFERENCES buildings(building_id) ON DELETE CASCADE,
    floor_number INT NOT NULL DEFAULT 1,
    apartment_name VARCHAR(100) NOT NULL,
    room_number VARCHAR(50) NOT NULL,
    unit_type VARCHAR(50),
    description VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_building_room UNIQUE (building_id, room_number)
);

CREATE INDEX IF NOT EXISTS idx_apartments_building ON apartments(building_id);
CREATE INDEX IF NOT EXISTS idx_apartments_floor ON apartments(building_id, floor_number);

-- 2. Create apartment_occupancies table
CREATE TABLE IF NOT EXISTS apartment_occupancies (
    occupancy_id BIGSERIAL PRIMARY KEY,
    apartment_id BIGINT NOT NULL REFERENCES apartments(apartment_id) ON DELETE CASCADE,
    resident_id BIGINT NOT NULL REFERENCES residents(resident_id) ON DELETE CASCADE,
    start_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    end_date TIMESTAMP,
    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
    move_out_reason VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_occupancy_apartment ON apartment_occupancies(apartment_id);
CREATE INDEX IF NOT EXISTS idx_occupancy_resident ON apartment_occupancies(resident_id);
CREATE INDEX IF NOT EXISTS idx_occupancy_status ON apartment_occupancies(status);

-- 3. Add apartment_id column to water_meters and resident_invitations
ALTER TABLE water_meters ADD COLUMN IF NOT EXISTS apartment_id BIGINT REFERENCES apartments(apartment_id) ON DELETE SET NULL;
ALTER TABLE resident_invitations ADD COLUMN IF NOT EXISTS apartment_id BIGINT REFERENCES apartments(apartment_id) ON DELETE SET NULL;
