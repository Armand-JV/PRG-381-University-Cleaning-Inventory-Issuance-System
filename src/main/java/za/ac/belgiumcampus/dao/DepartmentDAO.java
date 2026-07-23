package za.ac.belgiumcampus.dao;

import za.ac.belgiumcampus.DatabaseConnection;
import za.ac.belgiumcampus.model.Department;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DepartmentDAO {
    private final Connection conn;

    public DepartmentDAO(Connection conn) {
        this.conn = conn;
    }

    /**
     * No-arg constructor for read methods below, which open their own
     * connection per call (mirrors SupplierDAO / CleanerDAO).
     */
    public DepartmentDAO() {
        this.conn = null;
    }

    public void addDepartment(String name) throws SQLException {
        String sql = "INSERT INTO departments (department_name) VALUES (?)";
        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setString(1, name);
        stmt.executeUpdate();
    }

    public void listDepartments() throws SQLException {
        String sql = "SELECT department_id, department_name FROM departments";
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(sql);
        while (rs.next()) {
            System.out.println(rs.getInt("department_id") + " - " + rs.getString("department_name"));
        }
    }

    /**
     * Read-only lookup used to populate the "Department" dropdown on the
     * Cleaners Management screen. Opens its own connection so callers
     * don't need to manage one (mirrors SupplierDAO's read methods).
     */
    public List<Department> getAllDepartments() throws SQLException {
        String sql = "SELECT department_id, department_name FROM departments ORDER BY department_name";
        List<Department> departments = new ArrayList<>();
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement stmt = c.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                departments.add(new Department(
                        rs.getInt("department_id"),
                        rs.getString("department_name")
                ));
            }
        }
        return departments;
    }
}
