package za.ac.belgiumcampus.model;

/**
 * Domain object for university cleaning staff, used to populate and
 * drive CRUD on the Cleaners Management screen.
 */
public class Cleaner {
    private int cleanerId;
    private String fullName;
    private String phone;
    private String email;
    private int departmentId;
    private String departmentName;

    public Cleaner(int cleanerId, String fullName, String phone, String email,
                    int departmentId, String departmentName) {
        this.cleanerId = cleanerId;
        this.fullName = fullName;
        this.phone = phone;
        this.email = email;
        this.departmentId = departmentId;
        this.departmentName = departmentName;
    }

    public int getCleanerId() {
        return cleanerId;
    }
    public String getFullName() {
        return fullName;
    }
    public String getPhone() { return phone; }
    public String getEmail() { return email; }
    public int getDepartmentId() { return departmentId; }
    public String getDepartmentName() { return departmentName; }

    public void setFullName(String fullName) { this.fullName = fullName; }
    public void setPhone(String phone) { this.phone = phone; }
    public void setEmail(String email) { this.email = email; }
    public void setDepartmentId(int departmentId) { this.departmentId = departmentId; }
    public void setDepartmentName(String departmentName) { this.departmentName = departmentName; }

    /**
     * Overriding toString() means Cleaner objects can be dropped straight
     * into a JComboBox and display their name instead of a memory address.
     */
    @Override
    public String toString() {
        return fullName;
    }
}
