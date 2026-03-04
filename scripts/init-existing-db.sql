-- Creates two databases:
-- 1. flyway_fresh    - empty DB for Scenario 1 (fresh Flyway init)
-- 2. flyway_existing - DB with pre-existing tables for Scenario 2 (baseline + migrate)

CREATE DATABASE IF NOT EXISTS flyway_fresh;
CREATE DATABASE IF NOT EXISTS flyway_existing;

-- Simulate a production DB that was created BEFORE adopting Flyway.
-- These tables match V1 and V2 migrations (users + orders).
USE flyway_existing;

CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE orders (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    total_amount DECIMAL(10, 2) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_orders_user FOREIGN KEY (user_id) REFERENCES users(id)
);

-- Insert some sample data to prove tables existed before Flyway
INSERT INTO users (username, password_hash) VALUES ('admin', 'hash_abc123');
INSERT INTO users (username, password_hash) VALUES ('john', 'hash_def456');
INSERT INTO orders (user_id, total_amount, status) VALUES (1, 99.99, 'COMPLETED');
