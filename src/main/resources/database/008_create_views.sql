-- =========================================================
-- 008_create_views.sql
-- Purpose: Convenience views backing the Dashboard and Reports
--          screens so the Java DAOs can query simple, ready-made
--          result sets instead of repeating joins/filters everywhere.
-- =========================================================

-- Materials at or below their reorder level (Low-stock alerts / report)
CREATE OR REPLACE VIEW vw_low_stock_materials AS
SELECT
    m.material_id,
    m.material_name,
    m.quantity,
    m.reorder_level,
    m.unit_of_measure,
    s.supplier_name
FROM materials m
LEFT JOIN suppliers s ON s.supplier_id = m.supplier_id
WHERE m.is_active = TRUE
  AND m.quantity <= m.reorder_level;

-- Full issuance history with human-readable names (Issuance history report)
CREATE OR REPLACE VIEW vw_issuance_history AS
SELECT
    si.issuance_id,
    m.material_name,
    c.full_name  AS cleaner_name,
    u.full_name  AS issued_by_name,
    si.quantity_issued,
    si.issuance_date,
    si.notes
FROM stock_issuances si
JOIN materials m ON m.material_id = si.material_id
JOIN cleaners  c ON c.cleaner_id  = si.cleaner_id
LEFT JOIN users u ON u.user_id    = si.issued_by
ORDER BY si.issuance_date DESC;

-- Total quantity issued per material (Material usage report)
CREATE OR REPLACE VIEW vw_material_usage AS
SELECT
    m.material_id,
    m.material_name,
    COALESCE(SUM(si.quantity_issued), 0) AS total_issued
FROM materials m
LEFT JOIN stock_issuances si ON si.material_id = m.material_id
GROUP BY m.material_id, m.material_name;

-- Dashboard summary statistics
CREATE OR REPLACE VIEW vw_dashboard_summary AS
SELECT
    (SELECT COUNT(*) FROM materials WHERE is_active = TRUE)              AS total_materials,
    (SELECT COUNT(*) FROM vw_low_stock_materials)                        AS low_stock_items,
    (SELECT COUNT(*) FROM cleaners WHERE is_active = TRUE)                AS total_cleaners,
    (SELECT COUNT(*) FROM stock_issuances
        WHERE issuance_date >= CURRENT_DATE - INTERVAL '7 days')         AS recent_issuances;
