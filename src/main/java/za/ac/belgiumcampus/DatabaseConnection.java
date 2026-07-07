package za.ac.belgiumcampus;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DatabaseConnection {

    private static final Properties CONFIG = loadConfigProperties();
    private static final String DB_NAME = getConfigValue("DB_NAME", "cleaning_inventory");
    private static final String DB_USER = getConfigValue("DB_USER", "cleaning_admin");
    private static final String DB_PASSWORD = getConfigValue("DB_PASSWORD", "");
    private static final String DB_IP = getConfigValue("DB_IP", "");
    private static final String DB_PORT = getConfigValue("DB_PORT", "5432");

    private static final String URL = "jdbc:postgresql://" + DB_IP + ":" + DB_PORT + "/" + DB_NAME;

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, DB_USER, DB_PASSWORD);
    }

    private static String getConfigValue(String key, String defaultValue) {
        String environmentValue = System.getenv(key);
        if (environmentValue != null && !environmentValue.isBlank()) {
            return environmentValue.trim();
        }

        String fileValue = CONFIG.getProperty(key);
        if (fileValue != null && !fileValue.isBlank()) {
            return fileValue.trim();
        }

        return defaultValue;
    }

    private static Properties loadConfigProperties() {
        Properties properties = new Properties();

        try (InputStream input = DatabaseConnection.class
                .getClassLoader()
                .getResourceAsStream("config.properties")) {
            if (input != null) {
                properties.load(input);
            }
        } catch (IOException e) {
            System.err.println("Unable to read config.properties: " + e.getMessage());
        }

        return properties;
    }
}