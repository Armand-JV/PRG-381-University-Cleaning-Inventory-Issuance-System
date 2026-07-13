/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package za.ac.belgiumcampus.view;

import za.ac.belgiumcampus.dao.MaterialDAO;
import za.ac.belgiumcampus.dao.SupplierDAO;
import za.ac.belgiumcampus.exception.NegativeStockException;
import za.ac.belgiumcampus.exception.ValidationException;
import za.ac.belgiumcampus.model.Material;
import za.ac.belgiumcampus.model.Supplier;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author ajver
 */
public class MaterialsFrame extends javax.swing.JFrame {

    private static final Logger logger = Logger.getLogger(MaterialsFrame.class.getName());

    private static final Color LOW_STOCK_ROW_COLOR = new Color(0xFD, 0xE2, 0xE1);
    private static final Color LOW_STOCK_ROW_COLOR_SELECTED = new Color(0xF6, 0xB8, 0xB5);

    private final MaterialDAO materialDAO = new MaterialDAO();
    private final SupplierDAO supplierDAO = new SupplierDAO();
    private final MaterialsTableModel tableModel = new MaterialsTableModel();

    private Integer selectedMaterialId = null; // null while adding a new material

    /**
     * Creates new form MaterialsFrame
     */
    public MaterialsFrame() {
        initComponents();
        setLocationRelativeTo(null);
        customizeComponents();
        loadSuppliers();
        refreshTable();
    }

    /**
     * Everything the GUI Builder can't express as a design-time property:
     * the custom low-stock row renderer, the table's row-selection listener,
     * digit-only guards on the numeric fields, and making the unit combo
     * editable. Called once, right after initComponents().
     */
    private void customizeComponents() {
        table.setDefaultRenderer(Object.class, new LowStockRowRenderer());
        table.setRowHeight(24);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                onRowSelected();
            }
        });

        restrictToNumeric(txtQuantity, false);
        restrictToNumeric(txtReorderLevel, false);
        restrictToNumeric(txtUnitPrice, true);
    }

    // ---------------------------------------------------------------
    // Data loading
    // ---------------------------------------------------------------

    private void loadSuppliers() {
        cbSupplier.removeAllItems();
        cbSupplier.addItem(null); // "no supplier assigned"
        try {
            List<Supplier> suppliers = supplierDAO.getAllActive();
            for (Supplier s : suppliers) {
                cbSupplier.addItem(s);
            }
        } catch (SQLException e) {
            logger.log(Level.WARNING, "Unable to load suppliers", e);
            setStatus("Could not load supplier list: " + e.getMessage(), true);
        }
    }

    private void refreshTable() {
        try {
            List<Material> materials = materialDAO.getAll();
            tableModel.setMaterials(materials);
            setStatus(materials.size() + " material(s) loaded.", false);
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Unable to load materials", e);
            setStatus("Could not load materials: " + e.getMessage(), true);
        }
    }

    private void onSearch() {
        try {
            String keyword = txtSearch.getText().trim();
            List<Material> materials;

            if (chkLowStockOnly.isSelected()) {
                materials = materialDAO.getLowStockMaterials();
                if (!keyword.isEmpty()) {
                    materials.removeIf(m -> !m.getMaterialName().toLowerCase().contains(keyword.toLowerCase()));
                }
            } else if (keyword.isEmpty()) {
                materials = materialDAO.getAll();
            } else {
                materials = materialDAO.search(keyword);
            }

            tableModel.setMaterials(materials);
            setStatus(materials.size() + " material(s) match your filter.", false);
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Search failed", e);
            setStatus("Search failed: " + e.getMessage(), true);
        }
    }

    // ---------------------------------------------------------------
    // CRUD actions
    // ---------------------------------------------------------------

    private void onAdd() {
        try {
            Material material = readFormIntoMaterial();
            materialDAO.add(material);
            setStatus("Material \"" + material.getMaterialName() + "\" added.", false);
            clearForm();
            refreshTable();
        } catch (NegativeStockException e) {
            showValidationError(e.getMessage());
        } catch (ValidationException e) {
            showValidationError(e.getMessage());
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Unable to add material", e);
            showDatabaseError(e);
        }
    }

    private void onUpdate() {
        if (selectedMaterialId == null) {
            setStatus("Select a material in the table before updating.", true);
            return;
        }
        try {
            Material material = readFormIntoMaterial();
            material.setMaterialId(selectedMaterialId);
            materialDAO.update(material);
            setStatus("Material \"" + material.getMaterialName() + "\" updated.", false);
            clearForm();
            refreshTable();
        } catch (NegativeStockException e) {
            showValidationError(e.getMessage());
        } catch (ValidationException e) {
            showValidationError(e.getMessage());
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Unable to update material", e);
            showDatabaseError(e);
        }
    }

    private void onDelete() {
        if (selectedMaterialId == null) {
            setStatus("Select a material in the table before deleting.", true);
            return;
        }
        String name = txtMaterialName.getText();
        int confirm = JOptionPane.showConfirmDialog(this,
                "Remove \"" + name + "\" from active inventory?\n"
                + "Past issuance history for this material is kept.",
                "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            materialDAO.delete(selectedMaterialId);
            setStatus("Material \"" + name + "\" removed from active inventory.", false);
            clearForm();
            refreshTable();
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Unable to delete material", e);
            showDatabaseError(e);
        }
    }

    private void onRowSelected() {
        int viewRow = table.getSelectedRow();
        if (viewRow < 0) {
            return;
        }
        int modelRow = table.convertRowIndexToModel(viewRow);
        Material m = tableModel.getMaterialAt(modelRow);

        selectedMaterialId = m.getMaterialId();
        txtMaterialName.setText(m.getMaterialName());
        txtDescription.setText(m.getDescription() == null ? "" : m.getDescription());
        cbUnitOfMeasure.setSelectedItem(m.getUnitOfMeasure());
        txtQuantity.setText(String.valueOf(m.getQuantity()));
        txtReorderLevel.setText(String.valueOf(m.getReorderLevel()));
        txtUnitPrice.setText(m.getUnitPrice().toPlainString());
        selectSupplierInCombo(m.getSupplierId());
    }

    private void selectSupplierInCombo(Integer supplierId) {
        if (supplierId == null) {
            cbSupplier.setSelectedItem(null);
            return;
        }
        for (int i = 0; i < cbSupplier.getItemCount(); i++) {
            Supplier s = cbSupplier.getItemAt(i);
            if (s != null && s.getSupplierId() == supplierId) {
                cbSupplier.setSelectedIndex(i);
                return;
            }
        }
    }

    private void clearForm() {
        selectedMaterialId = null;
        txtMaterialName.setText("");
        txtDescription.setText("");
        cbUnitOfMeasure.setSelectedIndex(0);
        txtQuantity.setText("");
        txtReorderLevel.setText("");
        txtUnitPrice.setText("");
        cbSupplier.setSelectedItem(null);
        table.clearSelection();
    }

    // ---------------------------------------------------------------
    // Form -> model, with field-level validation
    // ---------------------------------------------------------------

    private Material readFormIntoMaterial() throws ValidationException {
        String name = txtMaterialName.getText().trim();
        String description = txtDescription.getText().trim();
        Object unitObj = cbUnitOfMeasure.getEditor().getItem();
        String unit = unitObj == null ? "" : unitObj.toString().trim();

        if (name.isEmpty()) {
            throw new ValidationException("Material name is required.");
        }
        if (unit.isEmpty()) {
            throw new ValidationException("Unit of measure is required.");
        }

        int quantity = parseNonNegativeInt(txtQuantity.getText(), "Quantity");
        int reorderLevel = parseNonNegativeInt(txtReorderLevel.getText(), "Reorder level");
        BigDecimal unitPrice = parseNonNegativeDecimal(txtUnitPrice.getText());

        Material material = new Material();
        material.setMaterialName(name);
        material.setDescription(description.isEmpty() ? null : description);
        material.setUnitOfMeasure(unit);
        material.setQuantity(quantity);
        material.setReorderLevel(reorderLevel);
        material.setUnitPrice(unitPrice);

        Supplier supplier = (Supplier) cbSupplier.getSelectedItem();
        material.setSupplierId(supplier == null ? null : supplier.getSupplierId());

        return material;
    }

    private int parseNonNegativeInt(String text, String fieldLabel) throws ValidationException {
        if (text == null || text.trim().isEmpty()) {
            throw new ValidationException(fieldLabel + " is required.");
        }
        try {
            int value = Integer.parseInt(text.trim());
            if (value < 0) {
                throw new NegativeStockException(fieldLabel + " cannot be negative.");
            }
            return value;
        } catch (NumberFormatException e) {
            throw new ValidationException(fieldLabel + " must be a whole number.");
        }
    }

    private BigDecimal parseNonNegativeDecimal(String text) throws ValidationException {
        if (text == null || text.trim().isEmpty()) {
            throw new ValidationException("Unit price is required.");
        }
        try {
            BigDecimal value = new BigDecimal(text.trim());
            if (value.compareTo(BigDecimal.ZERO) < 0) {
                throw new ValidationException("Unit price cannot be negative.");
            }
            return value;
        } catch (NumberFormatException e) {
            throw new ValidationException("Unit price must be a valid amount (e.g. 49.99).");
        }
    }

    /** Restricts a text field to digits (and optionally one decimal point). */
    private void restrictToNumeric(JTextField field, boolean allowDecimal) {
        field.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                char c = e.getKeyChar();
                boolean isDigit = Character.isDigit(c);
                boolean isDecimalPoint = allowDecimal && c == '.' && !field.getText().contains(".");
                boolean isControl = Character.isISOControl(c);
                if (!isDigit && !isDecimalPoint && !isControl) {
                    e.consume();
                }
            }
        });
    }

    // ---------------------------------------------------------------
    // Status / error feedback
    // ---------------------------------------------------------------

    private void setStatus(String message, boolean isError) {
        lblStatus.setText(message);
        lblStatus.setForeground(isError ? new Color(0xB3, 0x1E, 0x1E) : new Color(0x1E, 0x6B, 0x2E));
    }

    private void showValidationError(String message) {
        setStatus(message, true);
        JOptionPane.showMessageDialog(this, message, "Validation Error", JOptionPane.WARNING_MESSAGE);
    }

    private void showDatabaseError(SQLException e) {
        String message = "A database error occurred: " + e.getMessage();
        setStatus(message, true);
        JOptionPane.showMessageDialog(this, message, "Database Error", JOptionPane.ERROR_MESSAGE);
    }

    /** Tints every cell in a low-stock row so the alert is obvious at a glance. */
    private class LowStockRowRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable tbl, Object value, boolean isSelected,
                                                         boolean hasFocus, int row, int column) {
            Component c = super.getTableCellRendererComponent(tbl, value, isSelected, hasFocus, row, column);
            int modelRow = tbl.convertRowIndexToModel(row);
            boolean lowStock = tableModel.getMaterialAt(modelRow).isLowStock();

            if (lowStock) {
                c.setBackground(isSelected ? LOW_STOCK_ROW_COLOR_SELECTED : LOW_STOCK_ROW_COLOR);
                c.setForeground(Color.BLACK);
            } else {
                c.setBackground(isSelected ? tbl.getSelectionBackground() : Color.WHITE);
                c.setForeground(Color.BLACK);
            }
            return c;
        }
    }

    /**
     * This method is called from within the constructor to initialize the
     * form. WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        pnlHeading = new javax.swing.JPanel();
        lblHeading = new javax.swing.JLabel();
        lblSubheading = new javax.swing.JLabel();
        pnlForm = new javax.swing.JPanel();
        lblName = new javax.swing.JLabel();
        txtMaterialName = new javax.swing.JTextField();
        lblDescription = new javax.swing.JLabel();
        txtDescription = new javax.swing.JTextField();
        lblUnit = new javax.swing.JLabel();
        cbUnitOfMeasure = new javax.swing.JComboBox<>();
        lblQuantity = new javax.swing.JLabel();
        txtQuantity = new javax.swing.JTextField();
        lblReorder = new javax.swing.JLabel();
        txtReorderLevel = new javax.swing.JTextField();
        lblPrice = new javax.swing.JLabel();
        txtUnitPrice = new javax.swing.JTextField();
        lblSupplier = new javax.swing.JLabel();
        cbSupplier = new javax.swing.JComboBox<>();
        pnlButtons = new javax.swing.JPanel();
        btnAdd = new javax.swing.JButton();
        btnUpdate = new javax.swing.JButton();
        btnDelete = new javax.swing.JButton();
        btnClear = new javax.swing.JButton();
        pnlTable = new javax.swing.JPanel();
        pnlSearch = new javax.swing.JPanel();
        lblSearch = new javax.swing.JLabel();
        txtSearch = new javax.swing.JTextField();
        chkLowStockOnly = new javax.swing.JCheckBox();
        btnRefresh = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        table = new javax.swing.JTable(tableModel);
        lblStatus = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Materials Management - University Cleaning Inventory System");

        lblHeading.setFont(new java.awt.Font("Segoe UI", 1, 20)); // NOI18N
        lblHeading.setText("Materials Management");

        lblSubheading.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        lblSubheading.setForeground(java.awt.Color.darkGray);
        lblSubheading.setText("Add, update, delete and track cleaning materials in stock");

        javax.swing.GroupLayout pnlHeadingLayout = new javax.swing.GroupLayout(pnlHeading);
        pnlHeading.setLayout(pnlHeadingLayout);
        pnlHeadingLayout.setHorizontalGroup(
            pnlHeadingLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlHeadingLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlHeadingLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblHeading)
                    .addComponent(lblSubheading))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        pnlHeadingLayout.setVerticalGroup(
            pnlHeadingLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlHeadingLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lblHeading)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblSubheading)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pnlForm.setBorder(javax.swing.BorderFactory.createTitledBorder("Material Details"));

        lblName.setText("Material Name *");

        lblDescription.setText("Description");

        lblUnit.setText("Unit of Measure *");

        cbUnitOfMeasure.setEditable(true);
        cbUnitOfMeasure.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "unit", "bottle", "box", "litre", "kg", "pack", "roll" }));

        lblQuantity.setText("Quantity in Stock *");

        lblReorder.setText("Reorder Level *");

        lblPrice.setText("Unit Price (R) *");

        lblSupplier.setText("Supplier");

        pnlButtons.setLayout(new java.awt.GridLayout(2, 2, 6, 6));

        btnAdd.setText("Add");
        btnAdd.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddActionPerformed(evt);
            }
        });
        pnlButtons.add(btnAdd);

        btnUpdate.setText("Update");
        btnUpdate.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnUpdateActionPerformed(evt);
            }
        });
        pnlButtons.add(btnUpdate);

        btnDelete.setText("Delete");
        btnDelete.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDeleteActionPerformed(evt);
            }
        });
        pnlButtons.add(btnDelete);

        btnClear.setText("Clear");
        btnClear.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnClearActionPerformed(evt);
            }
        });
        pnlButtons.add(btnClear);

        javax.swing.GroupLayout pnlFormLayout = new javax.swing.GroupLayout(pnlForm);
        pnlForm.setLayout(pnlFormLayout);
        pnlFormLayout.setHorizontalGroup(
            pnlFormLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlFormLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlFormLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(pnlButtons, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(lblName)
                    .addComponent(txtMaterialName)
                    .addComponent(lblDescription)
                    .addComponent(txtDescription)
                    .addComponent(lblUnit)
                    .addComponent(cbUnitOfMeasure, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(lblQuantity)
                    .addComponent(txtQuantity)
                    .addComponent(lblReorder)
                    .addComponent(txtReorderLevel)
                    .addComponent(lblPrice)
                    .addComponent(txtUnitPrice)
                    .addComponent(lblSupplier)
                    .addComponent(cbSupplier, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        pnlFormLayout.setVerticalGroup(
            pnlFormLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlFormLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lblName)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(txtMaterialName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(lblDescription)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(txtDescription, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(lblUnit)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(cbUnitOfMeasure, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(lblQuantity)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(txtQuantity, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(lblReorder)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(txtReorderLevel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(lblPrice)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(txtUnitPrice, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(lblSupplier)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(cbSupplier, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(pnlButtons, javax.swing.GroupLayout.PREFERRED_SIZE, 70, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        lblSearch.setText("Search:");

        txtSearch.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtSearchActionPerformed(evt);
            }
        });

        chkLowStockOnly.setText("Show low-stock only");
        chkLowStockOnly.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chkLowStockOnlyActionPerformed(evt);
            }
        });

        btnRefresh.setText("Refresh");
        btnRefresh.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRefreshActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout pnlSearchLayout = new javax.swing.GroupLayout(pnlSearch);
        pnlSearch.setLayout(pnlSearchLayout);
        pnlSearchLayout.setHorizontalGroup(
            pnlSearchLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlSearchLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lblSearch)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(txtSearch, javax.swing.GroupLayout.PREFERRED_SIZE, 180, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(chkLowStockOnly)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnRefresh)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        pnlSearchLayout.setVerticalGroup(
            pnlSearchLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
            .addComponent(lblSearch)
            .addComponent(txtSearch, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addComponent(chkLowStockOnly)
            .addComponent(btnRefresh)
        );

        jScrollPane1.setViewportView(table);

        javax.swing.GroupLayout pnlTableLayout = new javax.swing.GroupLayout(pnlTable);
        pnlTable.setLayout(pnlTableLayout);
        pnlTableLayout.setHorizontalGroup(
            pnlTableLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(pnlSearch, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jScrollPane1)
        );
        pnlTableLayout.setVerticalGroup(
            pnlTableLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlTableLayout.createSequentialGroup()
                .addComponent(pnlSearch, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 460, Short.MAX_VALUE))
        );

        lblStatus.setBorder(javax.swing.BorderFactory.createEmptyBorder(4, 12, 8, 12));
        lblStatus.setText(" ");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(pnlHeading, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(pnlForm, javax.swing.GroupLayout.PREFERRED_SIZE, 300, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(pnlTable, javax.swing.GroupLayout.DEFAULT_SIZE, 660, Short.MAX_VALUE)
                .addContainerGap())
            .addComponent(lblStatus, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(pnlHeading, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(pnlForm, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(pnlTable, javax.swing.GroupLayout.DEFAULT_SIZE, 520, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblStatus, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnAddActionPerformed(java.awt.event.ActionEvent evt) {
        onAdd();
    }

    private void btnUpdateActionPerformed(java.awt.event.ActionEvent evt) {
        onUpdate();
    }

    private void btnDeleteActionPerformed(java.awt.event.ActionEvent evt) {
        onDelete();
    }

    private void btnClearActionPerformed(java.awt.event.ActionEvent evt) {
        clearForm();
    }

    private void btnRefreshActionPerformed(java.awt.event.ActionEvent evt) {
        txtSearch.setText("");
        chkLowStockOnly.setSelected(false);
        refreshTable();
    }

    private void chkLowStockOnlyActionPerformed(java.awt.event.ActionEvent evt) {
        onSearch();
    }

    private void txtSearchActionPerformed(java.awt.event.ActionEvent evt) {
        onSearch();
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new MaterialsFrame().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAdd;
    private javax.swing.JButton btnClear;
    private javax.swing.JButton btnDelete;
    private javax.swing.JButton btnRefresh;
    private javax.swing.JButton btnUpdate;
    private javax.swing.JComboBox<Supplier> cbSupplier;
    private javax.swing.JComboBox<String> cbUnitOfMeasure;
    private javax.swing.JCheckBox chkLowStockOnly;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel lblDescription;
    private javax.swing.JLabel lblHeading;
    private javax.swing.JLabel lblName;
    private javax.swing.JLabel lblPrice;
    private javax.swing.JLabel lblQuantity;
    private javax.swing.JLabel lblReorder;
    private javax.swing.JLabel lblSearch;
    private javax.swing.JLabel lblStatus;
    private javax.swing.JLabel lblSubheading;
    private javax.swing.JLabel lblSupplier;
    private javax.swing.JLabel lblUnit;
    private javax.swing.JPanel pnlButtons;
    private javax.swing.JPanel pnlForm;
    private javax.swing.JPanel pnlHeading;
    private javax.swing.JPanel pnlSearch;
    private javax.swing.JPanel pnlTable;
    private javax.swing.JTable table;
    private javax.swing.JTextField txtDescription;
    private javax.swing.JTextField txtMaterialName;
    private javax.swing.JTextField txtQuantity;
    private javax.swing.JTextField txtReorderLevel;
    private javax.swing.JTextField txtSearch;
    private javax.swing.JTextField txtUnitPrice;
    // End of variables declaration//GEN-END:variables
}
