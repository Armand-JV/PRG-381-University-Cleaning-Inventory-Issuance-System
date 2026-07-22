package za.ac.belgiumcampus.dao;

import za.ac.belgiumcampus.DatabaseConnection;
import za.ac.belgiumcampus.model.Cleaner;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CleanerDAO {
    private Connection conn;

    public CleanerDAO(Connection conn) {
        this.conn = conn;
    }

    /**
     * No-arg constructor for the CRUD methods below, which open their own
     * connection per call (mirrors SupplierDAO).
     */
    public CleanerDAO() {
        this.conn = null;
    }

    // CREATE
    public void addCleaner(String fullName, String phone, String email, int departmentId, boolean isActive) throws SQLException {
        String sql = "INSERT INTO cleaners (full_name, phone, email, department_id, is_active) VALUES (?, ?, ?, ?, ?)";
        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setString(1, fullName);
        stmt.setString(2, phone);
        stmt.setString(3, email);
        stmt.setInt(4, departmentId);
        stmt.setBoolean(5, isActive);
        stmt.executeUpdate();
    }

    // READ
    public void listCleaners() throws SQLException {
        String sql = "SELECT cleaner_id, full_name, email FROM cleaners";
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(sql);
        while (rs.next()) {
            System.out.println(rs.getInt("cleaner_id") + " - " + rs.getString("full_name") + " (" + rs.getString("email") + ")");
        }
    }

    public void addCleaner(Cleaner cleaner) throws SQLException {
        String sql = "INSERT INTO cleaners (full_name, phone, email, department_id, is_active) "
                + "VALUES (?, ?, ?, ?, TRUE)";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement stmt = c.prepareStatement(sql)) {
            stmt.setString(1, cleaner.getFullName());
            stmt.setString(2, cleaner.getPhone());
            stmt.setString(3, cleaner.getEmail());
            if (cleaner.getDepartmentId() > 0) {
                stmt.setInt(4, cleaner.getDepartmentId());
            } else {
                stmt.setNull(4, Types.INTEGER);
            }
            stmt.executeUpdate();
        }
    }

    public void updateCleaner(Cleaner cleaner) throws SQLException {
        String sql = "UPDATE cleaners SET full_name=?, phone=?, email=?, department_id=? "
                + "WHERE cleaner_id=?";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement stmt = c.prepareStatement(sql)) {
            stmt.setString(1, cleaner.getFullName());
            stmt.setString(2, cleaner.getPhone());
            stmt.setString(3, cleaner.getEmail());
            if (cleaner.getDepartmentId() > 0) {
                stmt.setInt(4, cleaner.getDepartmentId());
            } else {
                stmt.setNull(4, Types.INTEGER);
            }
            stmt.setInt(5, cleaner.getCleanerId());
            stmt.executeUpdate();
        }
    }

    public void deleteCleaner(int cleanerId) throws SQLException {
        String sql = "DELETE FROM cleaners WHERE cleaner_id=?";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement stmt = c.prepareStatement(sql)) {
            stmt.setInt(1, cleanerId);
            stmt.executeUpdate();
        }
    }

    public List<Cleaner> getAllCleaners() throws SQLException {
        String sql = "SELECT c.cleaner_id, c.full_name, c.phone, c.email, c.department_id, d.department_name "
                + "FROM cleaners c LEFT JOIN departments d ON c.department_id = d.department_id";
        List<Cleaner> cleaners = new ArrayList<>();
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement stmt = c.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                cleaners.add(new Cleaner(
                        rs.getInt("cleaner_id"),
                        rs.getString("full_name"),
                        rs.getString("phone"),
                        rs.getString("email"),
                        rs.getInt("department_id"),
                        rs.getString("department_name")
                ));
            }
        }
        return cleaners;
    }
}

