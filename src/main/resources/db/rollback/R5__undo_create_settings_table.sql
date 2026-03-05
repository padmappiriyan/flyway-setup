-- =============================================================
-- Rollback   : R5__undo_create_settings_table.sql
-- Description: Reverts changes made in V5 migration.
-- =============================================================

DROP TABLE IF EXISTS settings;
