-- V7: Secure Resident Invitation Entity & Token Hashing Migration

-- 1. Create resident_invitations table
CREATE TABLE IF NOT EXISTS resident_invitations (
    id BIGSERIAL PRIMARY KEY,
    resident_id BIGINT REFERENCES residents(resident_id) ON DELETE CASCADE,
    building_id BIGINT REFERENCES buildings(building_id) ON DELETE CASCADE,
    invited_by_id BIGINT REFERENCES building_owners(owner_id) ON DELETE SET NULL,
    resident_name VARCHAR(100) NOT NULL,
    resident_email VARCHAR(150) NOT NULL,
    phone_number VARCHAR(20),
    apartment_number VARCHAR(20) NOT NULL,
    token_hash VARCHAR(255) NOT NULL UNIQUE,
    expires_at TIMESTAMP NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    accepted_at TIMESTAMP
);

-- Indexes for optimal lookup performance
CREATE INDEX IF NOT EXISTS idx_invitations_token_hash ON resident_invitations(token_hash);
CREATE INDEX IF NOT EXISTS idx_invitations_resident_email ON resident_invitations(resident_email);
CREATE INDEX IF NOT EXISTS idx_invitations_building_id ON resident_invitations(building_id);
CREATE INDEX IF NOT EXISTS idx_invitations_status ON resident_invitations(status);

-- 2. Add security columns to residents table for backward compatibility
ALTER TABLE residents ADD COLUMN IF NOT EXISTS token_hash VARCHAR(255);
ALTER TABLE residents ADD COLUMN IF NOT EXISTS expires_at TIMESTAMP;
ALTER TABLE residents ADD COLUMN IF NOT EXISTS accepted_at TIMESTAMP;
