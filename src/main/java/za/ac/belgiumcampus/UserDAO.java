package za.ac.belgiumcampus;

import java.sql.*;

public class UserDAO {
    private final Connection conn;

    public UserDAO(Connection conn) {
        this.conn = conn;
    }

    // CREATE
    public void registerUser(String fullName, String username, String email, String passwordHash, String role) throws SQLException {
        String sql = "INSERT INTO users (full_name, username, email, password_hash, role) VALUES (?, ?, ?, ?, ?)";
        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setString(1, fullName);
        stmt.setString(2, username);
        stmt.setString(3, email);
        stmt.setString(4, passwordHash);
        stmt.setString(5, role);
        stmt.executeUpdate();
    }

    // READ
    public void listUsers() throws SQLException {
        String sql = "SELECT user_id, full_name, username, role FROM users";
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(sql);
        while (rs.next()) {
            System.out.println(rs.getInt("user_id") + " - " + rs.getString("full_name") +
                    " (" + rs.getString("username") + ") Role: " + rs.getString("role"));
        }
    }

    // UPDATE
    public void updateUserRole(int userId, String newRole) throws SQLException {
        String sql = "UPDATE users SET role = ? WHERE user_id = ?";
        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setString(1, newRole);
        stmt.setInt(2, userId);
        stmt.executeUpdate();
    }

    // DELETE
    public void deleteUser(int userId) throws SQLException {
        String sql = "DELETE FROM users WHERE user_id = ?";
        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setInt(1, userId);
        stmt.executeUpdate();
    }
}
