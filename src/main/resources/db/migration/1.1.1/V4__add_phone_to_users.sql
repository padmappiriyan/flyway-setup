-- ==============================================================
-- Migration  : V5__add_phone_to_users.sql
-- Version    : 1.1.1
-- Description: Adds a 'phone' column to the 'users' table.
-- ==============================================================

ALTER TABLE users ADD COLUMN phone VARCHAR(20) DEFAULT NULL;
