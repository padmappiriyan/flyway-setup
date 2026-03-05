-- =============================================================
-- Rollback   : R4__undo_create_products_and_order_items.sql
-- Author     : Developer
-- Date       : 2026-03-05
-- Description: Reverts changes made in V4 migration.
--              Drops 'order_items' first due to FK constraints,
--              then drops 'products'.
-- =============================================================

-- ── Step 1: Drop order_items table ───────────────────────────
-- (This also removes the indexes and FK constraints)
DROP TABLE IF EXISTS order_items;

-- ── Step 2: Drop products table ──────────────────────────────
DROP TABLE IF EXISTS products;
