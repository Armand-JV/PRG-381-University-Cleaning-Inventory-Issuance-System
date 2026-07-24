# Materials & Inventory Management Module

**Track B – Java Swing Desktop Application**
**Package:** `za.ac.belgiumcampus`

This document describes the Materials & Inventory Management module: what
it does, how it is built, and how to talk about it during the group
presentation and code-explanation demo.

---

## 1. What this module covers

| Requirement (from the project brief) | Where it's implemented |
|---|---|
| Add, view, update and delete cleaning materials | `MaterialsFrame` + `MaterialDAO` (add/getAll/update/delete) |
| Track available quantities | `materials.quantity` column, shown and edited in `MaterialsFrame` |
| Record reorder levels | `materials.reorder_level` column, editable per material |
| Search and filter materials | Search box (`MaterialDAO.search`) + "Show low-stock only" filter |
| Prevent negative stock values | `MaterialDAO.validate()` + inline digit-only text fields |
| Validate required fields | `MaterialDAO.validate()` + `MaterialsFrame.readFormIntoMaterial()` |
| Low-stock alerts | `MaterialDAO.getLowStockMaterials()`, red row highlighting, `vw_low_stock_materials` |

## 2. Files added for this module

```
src/main/java/za/ac/belgiumcampus/
├── model/
│   ├── Material.java          - domain object for one row in `materials`
│   └── Supplier.java          - minimal read-only model for the supplier dropdown
├── exception/
│   ├── ValidationException.java     - checked exception for failed business rules
│   └── NegativeStockException.java  - specific rule: quantity/reorder level going negative
├── dao/
│   ├── GenericDAO.java         - shared CRUD contract (abstraction)
│   ├── MaterialDAO.java        - JDBC CRUD, search, low-stock query, validation
│   └── SupplierDAO.java        - read-only lookup to populate the supplier dropdown
└── view/
    ├── MaterialsFrame.java       - the Materials Management screen
    └── MaterialsTableModel.java  - table model backing the inventory table
```

The database side (`materials` table, constraints, indexes, and the
`vw_low_stock_materials` view) already existed in
`004_create_materials.sql` and `008_create_views.sql` and did not need to
change for this module.

## 3. Design of the Materials Management screen

The screen is split into three areas:

- **Left – Material Details form:** name, description, unit of measure,
  quantity, reorder level, unit price, and supplier, with **Add**,
  **Update**, **Delete** and **Clear** buttons.
- **Center – Inventory table:** every active material, refreshed after
  every change. Clicking a row loads it into the form for editing.
- **Top of table – Search bar:** a live search box (filters as you type)
  and a **"Show low-stock only"** checkbox, plus a **Refresh** button.

Rows where `quantity <= reorder_level` are tinted red across the whole
row (`MaterialsFrame.LowStockRowRenderer`) and marked `LOW STOCK` in the
Status column, so low stock is visible at a glance without opening a
separate report.

## 4. Validation & business rules

Validation happens in two places on purpose (defence in depth, matching
the same pattern already used for stock issuance in
`006_create_stock_issuances.sql`):

1. **In the form** (`MaterialsFrame`): required fields can't be blank,
   numeric fields only accept digits (and one decimal point for price),
   and friendly messages appear in the status bar and a dialog.
2. **In the DAO** (`MaterialDAO.validate()`): re-checks the same rules
   before any SQL statement runs, so the rule holds even if another class
   calls `MaterialDAO` directly.

Two exception types back this:

- `ValidationException` – general validation failures (blank name,
  invalid unit price, etc.).
- `NegativeStockException extends ValidationException` – the specific
  rule that quantity or reorder level can never go negative. Kept as its
  own subclass so calling code could catch it separately in future if
  a different reaction (vs. a generic validation message) is ever needed.

## 5. "Delete" is a soft delete

`materials.material_id` is referenced by `stock_issuances` with an
`ON DELETE RESTRICT` foreign key (see `006_create_stock_issuances.sql`),
so a material that has ever been issued to a cleaner cannot be hard
deleted without destroying issuance history. `MaterialDAO.delete()`
therefore sets `is_active = FALSE` instead of removing the row. Deleted
materials disappear from the active list, the supplier-style dropdowns,
and every report, while issuance history for them stays intact.

## 6. Low-stock alert logic

A material is "low stock" the moment its `quantity` drops to, or below,
its `reorder_level` (`Material.isLowStock()`, mirrored by
`MaterialDAO.getLowStockMaterials()` and the `vw_low_stock_materials`
view). This single rule drives three things:

- red rows in the Materials table,
- the "Show low-stock only" filter,
- the Dashboard's low-stock count and the Low-Stock report (once those
  screens query the same DAO/view).
