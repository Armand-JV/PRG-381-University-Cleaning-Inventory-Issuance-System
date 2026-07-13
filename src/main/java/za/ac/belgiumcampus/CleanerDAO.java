package za.ac.belgiumcampus;

import java.sql.*;

public class CleanerDAO {
    private Connection conn;

    public CleanerDAO(Connection conn) {
        this.conn = conn;
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
}

