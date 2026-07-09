-- =========================================================
-- 002_create_suppliers.sql
-- Table: suppliers
-- Purpose: Suppliers of cleaning materials (Suppliers Management CRUD).
-- =========================================================

CREATE TABLE IF NOT EXISTS suppliers (
    supplier_id     SERIAL PRIMARY KEY,
    supplier_name   VARCHAR(150)    NOT NULL,
    contact_person  VARCHAR(100),
    phone           VARCHAR(20),
    email           VARCHAR(150),
    address         VARCHAR(255),
    is_active       BOOLEAN         NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT chk_suppliers_email CHECK (email IS NULL OR email ~* '^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$')
);

CREATE INDEX IF NOT EXISTS idx_suppliers_name ON suppliers (supplier_name);

COMMENT ON TABLE suppliers IS 'Companies/individuals that supply cleaning materials to the university.';
