package za.ac.belgiumcampus.model;

/**
 * Minimal domain object for departments, used to populate the
 * "Department" dropdown on the Cleaners Management screen.
 */
public class Department {
    private int departmentId;
    private String departmentName;

    public Department(int departmentId, String departmentName) {
        this.departmentId = departmentId;
        this.departmentName = departmentName;
    }

    public int getDepartmentId() {
        return departmentId;
    }

    public String getDepartmentName() {
        return departmentName;
    }

    /**
     * Overriding toString() means Department objects can be dropped straight
     * into a JComboBox and display their name instead of a memory address.
     */
    @Override
    public String toString() {
        return departmentName;
    }
}
