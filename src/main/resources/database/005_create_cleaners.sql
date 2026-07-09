-- =========================================================
-- 005_create_cleaners.sql
-- Table: cleaners
-- Purpose: University cleaning staff who receive issued materials
--          (Cleaners Management CRUD), optionally linked to a department.
-- =========================================================

CREATE TABLE IF NOT EXISTS cleaners (
    cleaner_id      SERIAL PRIMARY KEY,
    full_name       VARCHAR(100)    NOT NULL,
    phone           VARCHAR(20),
    email           VARCHAR(150),
    department_id   INTEGER,
    is_active       BOOLEAN         NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_cleaners_department
        FOREIGN KEY (department_id) REFERENCES departments (department_id)
        ON DELETE SET NULL,

    CONSTRAINT chk_cleaners_email CHECK (email IS NULL OR email ~* '^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$')
);

CREATE INDEX IF NOT EXISTS idx_cleaners_name          ON cleaners (full_name);   -- Search and filtering
CREATE INDEX IF NOT EXISTS idx_cleaners_department_id ON cleaners (department_id);

COMMENT ON TABLE cleaners IS 'University cleaning staff who cleaning materials are issued to.';
