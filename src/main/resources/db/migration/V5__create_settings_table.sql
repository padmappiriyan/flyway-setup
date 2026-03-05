-- =============================================================
-- Migration  : V5__create_settings_table.sql
-- Description: Creates a simple settings table for system-wide
--              configurations (key-value pairs).
-- =============================================================

CREATE TABLE settings (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    key_name      VARCHAR(50) NOT NULL UNIQUE,
    value_text    VARCHAR(255) NOT NULL,
    updated_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Insert a default maintenance mode setting
INSERT INTO settings (key_name, value_text) VALUES ('maintenance_mode', 'false');
