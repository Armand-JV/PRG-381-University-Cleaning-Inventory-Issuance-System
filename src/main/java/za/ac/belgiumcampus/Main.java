package za.ac.belgiumcampus;

import java.sql.Connection;
import java.sql.SQLException;

public class Main {
    public static void main(String[] args) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            if (conn != null && !conn.isClosed()) {
                System.out.println("Database connection successful.");
                DatabaseInitializer.runMigrations(conn);
                System.out.println("Database schema is up to date.");
            } else {
                System.out.println("Database connection failed.");
            }
        } catch (SQLException e) {
            System.err.println("Database connection failed: " + e.getMessage());
        }
    }
}