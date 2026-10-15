-- V12: Resident Apartment Movement and Tenancy History Schema

CREATE TABLE IF NOT EXISTS resident_history (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    owner_id BIGINT NOT NULL,
    building_id BIGINT,
    building_name VARCHAR(150),
    apartment_id BIGINT,
    apartment_name VARCHAR(150),
    room_number VARCHAR(50),
    resident_id BIGINT,
    resident_name VARCHAR(150) NOT NULL,
    resident_email VARCHAR(150),
    resident_phone VARCHAR(50),
    activity_type VARCHAR(50) NOT NULL,
    details VARCHAR(500),
    actor VARCHAR(100),
    timestamp DATETIME NOT NULL
);

CREATE INDEX idx_resident_hist_owner ON resident_history(owner_id);
CREATE INDEX idx_resident_hist_type ON resident_history(activity_type);
CREATE INDEX idx_resident_hist_time ON resident_history(timestamp);
