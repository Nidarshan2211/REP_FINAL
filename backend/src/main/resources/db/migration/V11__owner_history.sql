-- V11: Building Owner History Audit Schema

CREATE TABLE IF NOT EXISTS owner_history (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    owner_id BIGINT,
    owner_name VARCHAR(150) NOT NULL,
    email VARCHAR(150),
    phone VARCHAR(50),
    event_type VARCHAR(50) NOT NULL,
    details VARCHAR(500),
    actor VARCHAR(100),
    timestamp DATETIME NOT NULL
);

CREATE INDEX idx_owner_history_type ON owner_history(event_type);
CREATE INDEX idx_owner_history_time ON owner_history(timestamp);
