package za.ac.belgiumcampus.dao;

import za.ac.belgiumcampus.model.User;
import za.ac.belgiumcampus.DatabaseConnection;
import java.sql.*;

public class UserDAO {

    public boolean registerUser(User user) {
        String sql = "INSERT INTO users (username, email, password_hash, full_name, role) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, user.getUsername());
            pstmt.setString(2, user.getEmail());
            pstmt.setString(3, user.getPasswordHash());
            pstmt.setString(4, user.getFullName());
            pstmt.setString(5, user.getRole());

            int rows = pstmt.executeUpdate();
            return rows > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Looks up a user by username, but only returns accounts where
     * is_active = TRUE - a deactivated staff member should not be able
     * to log in even if they still know their correct password.
     */
    public User getUserByUsername(String username) {
        String sql = "SELECT * FROM users WHERE username = ? AND is_active = TRUE";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapRowToUser(rs);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Checks whether a username is already taken, regardless of whether
     * that account is active - a username should stay reserved even if
     * the account tied to it was deactivated, so no one else can register
     * with it.
     */
    public boolean usernameExists(String username) {
        String sql = "SELECT 1 FROM users WHERE username = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);

            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next();
            }

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Checks whether an email is already registered, regardless of
     * whether that account is active (same reasoning as usernameExists).
     */
    public boolean emailExists(String email) {
        String sql = "SELECT 1 FROM users WHERE email = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, email);

            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next();
            }

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Deactivates a user account instead of deleting the row outright -
     * preserves any historical records (e.g. stock issuances) tied to
     * this user_id via foreign key, while preventing further logins.
     */
    public boolean deactivateUser(int userId) {
        String sql = "UPDATE users SET is_active = FALSE, updated_at = NOW() WHERE user_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            int rows = pstmt.executeUpdate();
            return rows > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private User mapRowToUser(ResultSet rs) throws SQLException {
        User user = new User();
        user.setUserId(rs.getInt("user_id"));
        user.setUsername(rs.getString("username"));
        user.setEmail(rs.getString("email"));
        user.setPasswordHash(rs.getString("password_hash"));
        user.setFullName(rs.getString("full_name"));
        user.setRole(rs.getString("role"));
        return user;
    }
}