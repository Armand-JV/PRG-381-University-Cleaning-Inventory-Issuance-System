package za.ac.belgiumcampus;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import za.ac.belgiumcampus.dao.CleanerDAO;
import za.ac.belgiumcampus.dao.DepartmentDAO;
import za.ac.belgiumcampus.dao.StockIssuanceDAO;
import za.ac.belgiumcampus.dao.SupplierDAO;
import za.ac.belgiumcampus.dao.MaterialDAO;
import za.ac.belgiumcampus.dao.UserDAO;
import za.ac.belgiumcampus.model.Supplier;
import za.ac.belgiumcampus.model.Material;
import za.ac.belgiumcampus.model.User;

public class Main {
    public static void main(String[] args) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            if (conn != null && !conn.isClosed()) {
                System.out.println("Database connection successful.");
                DatabaseInitializer.runMigrations(conn);
                System.out.println("Database schema is up to date.");

                // SupplierDAO opens its own connections; use its read methods
                SupplierDAO supplierDAO = new SupplierDAO();
                try {
                    List<Supplier> suppliers = supplierDAO.getAllSuppliers();
                    System.out.println("Suppliers: " + suppliers.size());
                } catch (SQLException e) {
                    System.err.println("Failed to fetch suppliers: " + e.getMessage());
                }

                // Use CleanerDAO with the shared connection
                CleanerDAO cleanerDAO = new CleanerDAO(conn);
                try {
                    cleanerDAO.listCleaners();
                } catch (SQLException e) {
                    System.err.println("Failed to list cleaners: " + e.getMessage());
                }

                // MaterialDAO opens its own connections
                MaterialDAO materialDAO = new MaterialDAO();
                try {
                    List<Material> materials = materialDAO.getAll();
                    System.out.println("Materials: " + materials.size());
                } catch (SQLException e) {
                    System.err.println("Failed to fetch materials: " + e.getMessage());
                }

                // UserDAO provides lookup helpers; check for a sample account
                UserDAO userDAO = new UserDAO();
                try {
                    User admin = userDAO.getUserByUsername("admin");
                    System.out.println("Admin user found: " + (admin != null));
                } catch (Exception e) {
                    System.err.println("Failed to check users: " + e.getMessage());
                }

                StockIssuanceDAO issuanceDAO = new StockIssuanceDAO(conn);
                try {
                    issuanceDAO.listIssuances();
                } catch (SQLException e) {
                    System.err.println("Failed to list issuances: " + e.getMessage());
                }

                DepartmentDAO departmentDAO = new DepartmentDAO(conn);
                try {
                    departmentDAO.listDepartments();
                } catch (SQLException e) {
                    System.err.println("Failed to list departments: " + e.getMessage());
                }
               
                javax.swing.SwingUtilities.invokeLater(() ->
                        new za.ac.belgiumcampus.view.LoginFrame().setVisible(true));
                
            } else {
                System.out.println("Database connection failed.");
            }
        } catch (SQLException e) {
            System.err.println("Database connection failed: " + e.getMessage());
        }
    }
}
