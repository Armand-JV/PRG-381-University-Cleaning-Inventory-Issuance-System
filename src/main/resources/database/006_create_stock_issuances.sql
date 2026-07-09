-- =========================================================
-- 006_create_stock_issuances.sql
-- Table: stock_issuances
-- Purpose: Record of materials issued to cleaners (Stock Issuance
--          Management CRUD). Also implements the business rules:
--            - automatically deduct stock on issuance
--            - prevent issuing more stock than is available
--          via a trigger, so the rule holds regardless of which
--          part of the app writes to this table.
-- =========================================================

CREATE TABLE IF NOT EXISTS stock_issuances (
    issuance_id     SERIAL PRIMARY KEY,
    material_id     INTEGER         NOT NULL,
    cleaner_id      INTEGER         NOT NULL,
    issued_by       INTEGER,                     -- FK to users: staff member who processed the issuance
    quantity_issued INTEGER         NOT NULL,
    issuance_date   TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    notes           VARCHAR(255),
    created_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_issuance_material
        FOREIGN KEY (material_id) REFERENCES materials (material_id)
        ON DELETE RESTRICT,

    CONSTRAINT fk_issuance_cleaner
        FOREIGN KEY (cleaner_id) REFERENCES cleaners (cleaner_id)
        ON DELETE RESTRICT,

    CONSTRAINT fk_issuance_issued_by
        FOREIGN KEY (issued_by) REFERENCES users (user_id)
        ON DELETE SET NULL,

    CONSTRAINT chk_issuance_quantity_positive CHECK (quantity_issued > 0)
);

CREATE INDEX IF NOT EXISTS idx_issuance_material_id ON stock_issuances (material_id);
CREATE INDEX IF NOT EXISTS idx_issuance_cleaner_id  ON stock_issuances (cleaner_id);
CREATE INDEX IF NOT EXISTS idx_issuance_date        ON stock_issuances (issuance_date); -- Issuance history / reports

COMMENT ON TABLE stock_issuances IS 'History of cleaning materials issued to cleaners; drives stock deduction.';

-- ---------------------------------------------------------
-- Trigger function: deduct stock and block over-issuance
-- ---------------------------------------------------------
CREATE OR REPLACE FUNCTION fn_issue_stock_and_deduct()
RETURNS TRIGGER AS $$
DECLARE
    available_qty INTEGER;
BEGIN
    SELECT quantity INTO available_qty
    FROM materials
    WHERE material_id = NEW.material_id
    FOR UPDATE;

    IF available_qty IS NULL THEN
        RAISE EXCEPTION 'Material % does not exist', NEW.material_id;
    END IF;

    IF NEW.quantity_issued > available_qty THEN
        RAISE EXCEPTION 'Cannot issue % units of material %: only % in stock',
            NEW.quantity_issued, NEW.material_id, available_qty;
    END IF;

    UPDATE materials
    SET quantity   = quantity - NEW.quantity_issued,
        updated_at = CURRENT_TIMESTAMP
    WHERE material_id = NEW.material_id;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trg_issue_stock_and_deduct ON stock_issuances;

CREATE TRIGGER trg_issue_stock_and_deduct
    BEFORE INSERT ON stock_issuances
    FOR EACH ROW
    EXECUTE FUNCTION fn_issue_stock_and_deduct();
