package za.ac.belgiumcampus.model;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;

/**
 * Read-only table model for the Materials Management screen's inventory
 * table. Extending {@link AbstractTableModel} (rather than reshaping data
 * into a DefaultTableModel's Object[][]) keeps the JTable backed directly
 * by {@link Material} objects, so a selected row can be turned straight
 * back into the Material it represents.
 *
 * @author user
 */
public class MaterialsTableModel extends AbstractTableModel {

    private static final String[] COLUMN_NAMES = {
        "ID", "Material Name", "Unit", "Quantity", "Reorder Level", "Unit Price", "Supplier", "Status"
    };

    private List<Material> materials = new ArrayList<>();

    public void setMaterials(List<Material> materials) {
        this.materials = materials;
        fireTableDataChanged();
    }

    public Material getMaterialAt(int row) {
        return materials.get(row);
    }

    @Override
    public int getRowCount() {
        return materials.size();
    }

    @Override
    public int getColumnCount() {
        return COLUMN_NAMES.length;
    }

    @Override
    public String getColumnName(int column) {
        return COLUMN_NAMES[column];
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return false; // Editing happens through the form fields, not in-place
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        Material m = materials.get(rowIndex);
        switch (columnIndex) {
            case 0: return m.getMaterialId();
            case 1: return m.getMaterialName();
            case 2: return m.getUnitOfMeasure();
            case 3: return m.getQuantity();
            case 4: return m.getReorderLevel();
            case 5: return "R " + m.getUnitPrice().setScale(2, java.math.RoundingMode.HALF_UP);
            case 6: return m.getSupplierName() == null ? "-" : m.getSupplierName();
            case 7: return m.isLowStock() ? "LOW STOCK" : "OK";
            default: return null;
        }
    }
}
