-- V10: Resident Occupancy Requests and Approval Workflow Schema

CREATE TABLE IF NOT EXISTS occupancy_requests (
    request_id BIGSERIAL PRIMARY KEY,
    apartment_id BIGINT NOT NULL REFERENCES apartments(apartment_id) ON DELETE CASCADE,
    resident_id BIGINT NOT NULL REFERENCES residents(resident_id) ON DELETE CASCADE,
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    request_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    reviewed_at TIMESTAMP,
    reviewed_by VARCHAR(150),
    rejection_reason VARCHAR(255),
    notes VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_req_apartment ON occupancy_requests(apartment_id);
CREATE INDEX IF NOT EXISTS idx_req_resident ON occupancy_requests(resident_id);
CREATE INDEX IF NOT EXISTS idx_req_status ON occupancy_requests(status);
