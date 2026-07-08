package za.ac.belgiumcampus;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DatabaseConnection {

    // config.properties must be placed under src/main/resources (classpath root)
    private static final String CONFIG_FILE = "config.properties";
    private static final Properties CONFIG = loadConfigProperties();

    private static final String DB_NAME = getConfigValue("DB_NAME", "cleaning_inventory");
    private static final String DB_USER = getConfigValue("DB_USER", "cleaning_admin");
    private static final String DB_PASSWORD = getRequiredConfigValue("DB_PASSWORD");
    private static final String DB_IP = getRequiredConfigValue("DB_IP");
    private static final String DB_PORT = getConfigValue("DB_PORT", "5432");

    // Defaults to "require" now that the DB is reachable over the open internet
    // via port-forwarding rather than a VPN/LAN.
    private static final String DB_SSLMODE = getConfigValue("DB_SSLMODE", "require");

    // Prevents the app hanging indefinitely if the forwarded port is
    // unreachable (router down, ISP change, etc).
    private static final String DB_CONNECT_TIMEOUT = getConfigValue("DB_CONNECT_TIMEOUT", "10");

    private static final String URL = String.format(
            "jdbc:postgresql://%s:%s/%s?sslmode=%s&connectTimeout=%s",
            DB_IP, DB_PORT, DB_NAME, DB_SSLMODE, DB_CONNECT_TIMEOUT);

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, DB_USER, DB_PASSWORD);
    }

    private static String getConfigValue(String key, String defaultValue) {
        String envValue = System.getenv(key);
        if (envValue != null && !envValue.isBlank()) {
            return envValue.trim();
        }

        String fileValue = CONFIG.getProperty(key);
        if (fileValue != null && !fileValue.isBlank()) {
            return fileValue.trim();
        }

        return defaultValue;
    }

    private static String getRequiredConfigValue(String key) {
        String value = getConfigValue(key, null);
        if (value == null) {
            throw new IllegalStateException(
                "Missing required config value: " + key +
                " (set it as an environment variable or in " + CONFIG_FILE + ")");
        }
        return value;
    }

    private static Properties loadConfigProperties() {
        Properties properties = new Properties();

        try (InputStream input = DatabaseConnection.class.getClassLoader().getResourceAsStream(CONFIG_FILE)) {
            if (input != null) {
                properties.load(input);
            } else {
                System.err.println(CONFIG_FILE + " not found on classpath");
            }
        } catch (IOException e) {
            System.err.println("Unable to read " + CONFIG_FILE + ": " + e.getMessage());
        }

        return properties;
    }
}