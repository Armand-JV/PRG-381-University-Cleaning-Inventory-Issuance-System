-- =========================================================
-- 007_updated_at_triggers.sql
-- Purpose: Generic trigger that keeps each table's "updated_at"
--          column current whenever a row is modified, so the UI
--          can reliably show "last updated" info and reports can
--          rely on it for auditing.
-- =========================================================

CREATE OR REPLACE FUNCTION fn_set_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trg_users_updated_at ON users;
CREATE TRIGGER trg_users_updated_at
    BEFORE UPDATE ON users
    FOR EACH ROW
    EXECUTE FUNCTION fn_set_updated_at();

DROP TRIGGER IF EXISTS trg_suppliers_updated_at ON suppliers;
CREATE TRIGGER trg_suppliers_updated_at
    BEFORE UPDATE ON suppliers
    FOR EACH ROW
    EXECUTE FUNCTION fn_set_updated_at();

DROP TRIGGER IF EXISTS trg_materials_updated_at ON materials;
CREATE TRIGGER trg_materials_updated_at
    BEFORE UPDATE ON materials
    FOR EACH ROW
    EXECUTE FUNCTION fn_set_updated_at();

DROP TRIGGER IF EXISTS trg_cleaners_updated_at ON cleaners;
CREATE TRIGGER trg_cleaners_updated_at
    BEFORE UPDATE ON cleaners
    FOR EACH ROW
    EXECUTE FUNCTION fn_set_updated_at();
