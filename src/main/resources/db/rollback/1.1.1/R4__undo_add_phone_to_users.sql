-- ==============================================================
-- Rollback   : R5__undo_add_phone_to_users.sql
-- Version    : 1.1.1
-- Description: Removes the 'phone' column from the 'users' table.
-- ==============================================================

ALTER TABLE users DROP COLUMN phone;
