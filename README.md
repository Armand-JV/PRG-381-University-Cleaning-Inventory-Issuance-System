# University Cleaning Inventory & Issuance System

A Java Swing desktop application for managing university cleaning inventory, suppliers, cleaners, and stock issuance.

## Prerequisites

Before running the application, install the following software:

| Software | Version |
|----------|---------|
| Java JDK | 21 |
| Apache Maven | 3.9 or later |
| Git | Latest |
| Apache NetBeans | Latest (Recommended) |

After you make sure all the software above is installed
Run the following commands in root of the project folder(terminal)
```
mvn install
```
then
```
run mvn compile 
```

After that add the code to the application where fits 

# Very Important 
Creat a new folder under /src/main called "resources" then create a file called config.properties and copy the secrets(Ask Armand for the secrets) into that file.

The database should connect after that(via runnning Main.java)



# NetBeans Setup Guide

## Step 1: Install NetBeans

1. Go to https://netbeans.apache.org/front/main/download/
2. Download the latest version for your OS and run the installer.
3. When prompted for components, make sure **Java SE** is checked.
4. Open NetBeans once to confirm it launches.

## Step 2: Clone the repository

1. Open NetBeans → **Team → Git → Clone...**
2. Paste the repository URL.
3. Choose a destination folder, then click through the wizard (**Next → Next → Finish**).
4. When it finishes, click **Open Project**.
5. Get `config.properties` from a teammate separately (not in the repo) and place it at `src/main/resources/config.properties`.

## Step 3: Add a new frame

1. Right-click the `view` package(za.ac.belgiumcampus.view) → **New → JFrame Form...**
2. Name it (e.g. `MaterialsFrame`), click **Finish**.
3. Drag components from the **Palette** (right side) onto the form — Labels, Text Fields, Buttons, Tables, etc.
4. Rename each component before writing any code: right-click it → **Change Variable Name...**
   - Text fields: `txtMaterialName`
   - Labels: `lblMaterialName`
   - Buttons: `btnAdd`, `btnUpdate`, `btnDelete`
   - Tables: `tblMaterials`

## Step 4: Wire up events

1. Click a button once to select it (Design view).
2. In the **Properties** panel (bottom-right), switch to **Events**.
3. Find `actionPerformed`, type a method name (e.g. `btnAddActionPerformed`), press **Enter**.
4. NetBeans switches to **Source** view and generates the empty method, already wired to the button.

## Step 5: Write the source code
Switch to the **Source** tab.

- **Do NOT edit `initComponents()` by hand** — it gets overwritten every time you touch Design view.
- **Do** write your logic inside the event methods NetBeans generated (e.g. `btnAddActionPerformed`).
- **They put a comment there saying you can type there specifically**
FOR EXAMPLE:
```java
private void btnAddActionPerformed(java.awt.event.ActionEvent evt) {
    String name = txtMaterialName.getText().trim();

    if (name.isEmpty()) {
        javax.swing.JOptionPane.showMessageDialog(this,
                "Please enter a material name.", "Validation Error",
                javax.swing.JOptionPane.WARNING_MESSAGE);
        return;
    }

    // TODO: materialDAO.addMaterial(new Material(name, ...));
    // TODO: refreshTable();
}
```
# Database Migrations

SQL migration scripts for the University Cleaning Inventory & Issuance System (PostgreSQL).


## Files (run in this order)

| File | Table(s) / Purpose |
|---|---|
| `001_create_users.sql` | `users` — staff accounts, roles, login |
| `002_create_suppliers.sql` | `suppliers` — supplier contact details |
| `003_create_departments.sql` | `departments` — lookup for cleaner assignment |
| `004_create_materials.sql` | `materials` — inventory, quantity, reorder level |
| `005_create_cleaners.sql` | `cleaners` — cleaning staff records |
| `006_create_stock_issuances.sql` | `stock_issuances` — issuance history + trigger that deducts stock and blocks over-issuing |
| `007_updated_at_triggers.sql` | Keeps `updated_at` columns current on every update |
| `008_create_views.sql` | Views for Dashboard summary and Reports (low stock, issuance history, material usage) |
| `run_all.sql` | Runs all of the above in order via `psql` |


## Entity relationships

```
users            (standalone — staff who log in / issue stock)
suppliers ──┐
            ├─▶ materials ──▶ stock_issuances ◀── cleaners
departments ────────────────────────────────▶ cleaners
```

- A **material** optionally belongs to a **supplier**.
- A **cleaner** optionally belongs to a **department**.
- A **stock_issuance** links one **material** + one **cleaner**, and optionally records which **user** processed it.

## Business rules enforced at the database level

- Duplicate `username` / `email` on `users` → blocked by unique constraints.
- Negative stock (`materials.quantity`, `reorder_level`) → blocked by `CHECK` constraints.
- Issuing more stock than is available → blocked by the trigger in `006_create_stock_issuances.sql`, which also auto-deducts stock on every issuance.
- Low-stock items, dashboard counts, issuance history, and material usage are all pre-built as views in `008_create_views.sql` so your DAO/report code can just `SELECT * FROM vw_...`.

