-- =========================================================
-- run_all.sql
-- Convenience script: applies every migration in this folder,
-- in the correct dependency order.
--
-- Usage (psql):
--   psql -h <DB_IP> -U cleaning_admin -d cleaning_inventory -f run_all.sql
-- =========================================================

\i 001_create_users.sql
\i 002_create_suppliers.sql
\i 003_create_departments.sql
\i 004_create_materials.sql
\i 005_create_cleaners.sql
\i 006_create_stock_issuances.sql
\i 007_updated_at_triggers.sql
\i 008_create_views.sql
