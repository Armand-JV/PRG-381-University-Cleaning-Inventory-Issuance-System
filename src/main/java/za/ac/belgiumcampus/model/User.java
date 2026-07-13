package za.ac.belgiumcampus.model;

/**
 * Represents a staff member (Storekeeper or Supervisor) in the system.
 *
 * Field names/types match what UserDAO reads from and writes to the
 * `users` table: user_id, username, email, password_hash, full_name, role.
 */
public class User {

    private int userId;
    private String username;
    private String email;
    private String passwordHash;
    private String fullName;
    private String role; // "Storekeeper" or "Supervisor"

    public User() {
        // Needed by UserDAO.getUserByUsername(), which builds a User via
        // the no-arg constructor and then sets fields one at a time.
    }

    public User(String fullName, String username, String email, String passwordHash, String role) {
        this.fullName = fullName;
        this.username = username;
        this.email = email;
        this.passwordHash = passwordHash;
        this.role = role;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    /** Convenience check used for role-based access control. */
    public boolean isSupervisor() {
        return "Supervisor".equalsIgnoreCase(role);
    }

    /** Convenience check used for role-based access control. */
    public boolean isStorekeeper() {
        return "Storekeeper".equalsIgnoreCase(role);
    }

    @Override
    public String toString() {
        return "User{" +
                "userId=" + userId +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", fullName='" + fullName + '\'' +
                ", role='" + role + '\'' +
                '}';
    }
}