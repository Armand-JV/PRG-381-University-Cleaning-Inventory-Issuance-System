-- =========================================================
-- 003_create_departments.sql
-- Table: departments
-- Purpose: Optional lookup table so Cleaners can be assigned to a
--          department (marked as optional in the project brief).
-- =========================================================

CREATE TABLE IF NOT EXISTS departments (
    department_id   SERIAL PRIMARY KEY,
    department_name VARCHAR(100)    NOT NULL,
    created_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uq_departments_name UNIQUE (department_name)
);

COMMENT ON TABLE departments IS 'University departments that cleaners can optionally be assigned to.';

-- A few sensible defaults so the dropdown in the UI is not empty out of the box
INSERT INTO departments (department_name)
VALUES ('General Cleaning'), ('Residence Halls'), ('Academic Buildings'), ('Administration')
ON CONFLICT (department_name) DO NOTHING;
