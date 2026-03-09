-- =============================================================
-- Migration  : V4__create_tags_table.sql
-- Description: Creates the 'tags' table.
-- =============================================================

CREATE TABLE tags (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    color VARCHAR(20) DEFAULT '#FFFFFF'
);
