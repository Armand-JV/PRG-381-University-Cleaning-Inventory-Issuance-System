-- =========================================================
-- 004_create_materials.sql
-- Table: materials
-- Purpose: Cleaning materials inventory (Materials Management CRUD),
--          quantity tracking, and reorder levels for low-stock alerts.
-- =========================================================

CREATE TABLE IF NOT EXISTS materials (
    material_id     SERIAL PRIMARY KEY,
    material_name   VARCHAR(150)    NOT NULL,
    description     VARCHAR(255),
    unit_of_measure VARCHAR(30)     NOT NULL DEFAULT 'unit',   -- e.g. bottle, box, litre, kg
    quantity        INTEGER         NOT NULL DEFAULT 0,
    reorder_level   INTEGER         NOT NULL DEFAULT 0,
    unit_price      NUMERIC(10,2)   NOT NULL DEFAULT 0.00,
    supplier_id     INTEGER,
    is_active       BOOLEAN         NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_materials_supplier
        FOREIGN KEY (supplier_id) REFERENCES suppliers (supplier_id)
        ON DELETE SET NULL,

    CONSTRAINT chk_materials_quantity_nonneg      CHECK (quantity >= 0),       -- Prevent negative stock values
    CONSTRAINT chk_materials_reorder_nonneg       CHECK (reorder_level >= 0),
    CONSTRAINT chk_materials_unit_price_nonneg    CHECK (unit_price >= 0)
);

CREATE INDEX IF NOT EXISTS idx_materials_name        ON materials (material_name);   -- Search and filtering
CREATE INDEX IF NOT EXISTS idx_materials_supplier_id ON materials (supplier_id);
CREATE INDEX IF NOT EXISTS idx_materials_low_stock   ON materials (quantity, reorder_level); -- Low-stock alerts/report

COMMENT ON TABLE materials IS 'Cleaning materials held in inventory, including stock levels and reorder thresholds.';
