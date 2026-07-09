package za.ac.belgiumcampus;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Runs the SQL migration files found under {@code src/main/resources/database}
 * against the given connection, in the correct dependency order.
 *
 * This is the programmatic equivalent of running database/run_all.sql
 * through psql, but it works entirely through JDBC so it can be executed
 * automatically every time the application starts (each script uses
 * IF NOT EXISTS / CREATE OR REPLACE, so running it repeatedly is safe).
 */
public class DatabaseInitializer {

    private static final Logger logger = Logger.getLogger(DatabaseInitializer.class.getName());

    private static final String RESOURCE_FOLDER = "database/";

    // Order matters: later files reference tables created by earlier ones.
    private static final String[] MIGRATION_FILES = {
        "001_create_users.sql",
        "002_create_suppliers.sql",
        "003_create_departments.sql",
        "004_create_materials.sql",
        "005_create_cleaners.sql",
        "006_create_stock_issuances.sql",
        "007_updated_at_triggers.sql",
        "008_create_views.sql"
    };

    private DatabaseInitializer() {
        // Utility class - not meant to be instantiated
    }

    /**
     * Executes every migration file in {@link #MIGRATION_FILES} against the
     * supplied connection, in order.
     *
     * @param conn an open database connection
     * @throws SQLException if any statement fails to execute
     */
    public static void runMigrations(Connection conn) throws SQLException {
        logger.info("Running database migrations...");

        for (String fileName : MIGRATION_FILES) {
            runScript(conn, fileName);
        }

        logger.info("Database migrations completed successfully.");
    }

    private static void runScript(Connection conn, String fileName) throws SQLException {
        String sql = readResource(fileName);
        List<String> statements = splitStatements(sql);

        try (Statement stmt = conn.createStatement()) {
            for (String statement : statements) {
                stmt.execute(statement);
            }
            logger.info(() -> "Applied migration: " + fileName);
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Failed to apply migration: " + fileName, e);
            throw e;
        }
    }

    private static String readResource(String fileName) {
        String path = RESOURCE_FOLDER + fileName;

        try (InputStream input = DatabaseInitializer.class.getClassLoader().getResourceAsStream(path)) {
            if (input == null) {
                throw new IllegalStateException(
                    "Migration file not found on classpath: " + path +
                    " (expected under src/main/resources/database)");
            }
            return new String(input.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException("Unable to read migration file: " + path, e);
        }
    }

    /**
     * Splits a SQL script into individual statements on ';', while ignoring
     * semicolons that appear:
     *  - inside a single-quoted string literal, e.g. 'issued to cleaners; etc.'
     *    (including the standard '' escaped-quote sequence), and
     *  - inside a pair of {@code $$} markers (used for PL/pgSQL function
     *    bodies), so semicolons inside a function body do not cause a
     *    premature split.
     */
    static List<String> splitStatements(String sql) {
        List<String> statements = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inDollarQuote = false;
        boolean inSingleQuote = false;

        int i = 0;
        int length = sql.length();

        while (i < length) {
            char c = sql.charAt(i);

            if (!inSingleQuote && c == '$' && i + 1 < length && sql.charAt(i + 1) == '$') {
                inDollarQuote = !inDollarQuote;
                current.append("$$");
                i += 2;
                continue;
            }

            if (!inDollarQuote && c == '\'') {
                if (inSingleQuote && i + 1 < length && sql.charAt(i + 1) == '\'') {
                    current.append("''");
                    i += 2;
                    continue;
                }
                inSingleQuote = !inSingleQuote;
                current.append(c);
                i++;
                continue;
            }

            if (c == ';' && !inDollarQuote && !inSingleQuote) {
                addIfNotBlank(statements, current.toString());
                current.setLength(0);
                i++;
                continue;
            }

            current.append(c);
            i++;
        }

        addIfNotBlank(statements, current.toString());
        return statements;
    }

    private static void addIfNotBlank(List<String> statements, String candidate) {
        String trimmed = candidate.trim();
        if (!trimmed.isEmpty()) {
            statements.add(trimmed);
        }
    }
}