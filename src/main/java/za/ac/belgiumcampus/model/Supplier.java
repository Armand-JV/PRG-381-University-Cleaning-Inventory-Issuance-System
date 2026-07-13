package za.ac.belgiumcampus.model;

/**
 * Minimal domain object for suppliers, used here only to populate the
 * "Supplier" dropdown on the Materials Management screen. Full CRUD for
 * suppliers (contact person, phone, address, etc.) belongs to the
 * Suppliers Management module owned by another teammate; this class
 * intentionally only carries what the Materials module needs.
 *
 * @author user
 */
public class Supplier {

    private int supplierId;
    private String supplierName;

    public Supplier(int supplierId, String supplierName) {
        this.supplierId = supplierId;
        this.supplierName = supplierName;
    }

    public int getSupplierId() {
        return supplierId;
    }

    public String getSupplierName() {
        return supplierName;
    }

    /**
     * Overriding toString() means Supplier objects can be dropped straight
     * into a JComboBox and display their name instead of a memory address -
     * a small, practical example of polymorphism (Object::toString overridden).
     */
    @Override
    public String toString() {
        return supplierName;
    }
}
