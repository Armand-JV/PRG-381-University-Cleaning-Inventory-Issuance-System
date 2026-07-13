package za.ac.belgiumcampus;

import za.ac.belgiumcampus.view.LoginFrame;
import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        try {
            // Set nice look and feel
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

            // Initialize Database
            System.out.println("Initializing database...");
            DatabaseInitializer.runMigrations(DatabaseConnection.getConnection());
            System.out.println("Database ready.");

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, 
                "Database initialization failed: " + e.getMessage(), 
                "Error", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }

        // Launch Login Screen
        SwingUtilities.invokeLater(() -> {
            new LoginFrame().setVisible(true);
        });
    }
}