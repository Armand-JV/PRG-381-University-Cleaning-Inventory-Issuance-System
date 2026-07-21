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
    private String contactPerson;
    private String phone;
    private String email;
    private String location;

    // Constructors
    public Supplier(int supplierId, String supplierName, String contactPerson,
                    String phone, String email, String location) {
        this.supplierId = supplierId;
        this.supplierName = supplierName;
        this.contactPerson = contactPerson;
        this.phone = phone;
        this.email = email;
        this.location = location;
    }



    public int getSupplierId() {
        return supplierId;
    }
    public String getSupplierName() {
        return supplierName;
    }
    public String getContactPerson() {return contactPerson;}
    public String getPhone() {return phone;}
    public String getEmail() {return email;}
    public String getLocation() {return location;}

    /**
     * Overriding toString() means Supplier objects can be dropped straight
     * into a JComboBox and display their name instead of a memory address -
     * a small, practical example of polymorphism (Object::toString overridden).
     */
    @Override
    public String toString() {
        return supplierName;
    }

    public void setSupplierName(String supplierName) { this.supplierName = supplierName; }
    public void setContactPerson(String contactPerson) { this.contactPerson = contactPerson; }
    public void setPhone(String phone) { this.phone = phone; }
    public void setEmail(String email) { this.email = email; }
    public void setLocation(String location) { this.location = location; }

}
