package dao;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Central JDBC connection factory for the ExpenseTracker application.
 *
 * <h3>Design decisions</h3>
 * <ul>
 *   <li><b>No UI dependency</b> – this class lives in the {@code dao} layer
 *       and must never import anything from {@code javax.swing} or
 *       {@code ui.*}.</li>
 *   <li><b>Externalised credentials</b> – host, port, database, username and
 *       password are all read from {@code config/db.properties} at runtime.
 *       Only the properties-file path is a compile-time constant.</li>
 *   <li><b>No connection pool (Phase 1)</b> – each call to
 *       {@link #getConnection()} opens a fresh physical connection. A pool
 *       (HikariCP, c3p0) can be layered in later without touching callers
 *       because the public API stays the same.</li>
 *   <li><b>Thread safety</b> – loading properties is done once in a
 *       {@code synchronized} static initialiser; individual
 *       {@code getConnection()} calls are independent and safe to invoke
 *       from SwingWorker background threads.</li>
 * </ul>
 *
 * <h3>Usage</h3>
 * <pre>{@code
 * try (Connection conn = DatabaseConnection.getConnection()) {
 *     // use conn …
 * } catch (SQLException e) {
 *     // handle …
 * }
 * }</pre>
 */
public final class DatabaseConnection {

    // -----------------------------------------------------------------
    // Configuration
    // -----------------------------------------------------------------

    /**
     * Path to the properties file, resolved relative to the JVM's working
     * directory (i.e. the project root when launched from there).
     * Override via the system property {@code db.config.path} if needed:
     * {@code java -Ddb.config.path=/etc/expensetracker/db.properties …}
     */
    private static final String DEFAULT_CONFIG_PATH = "config/db.properties";

    private static final String PROP_DRIVER   = "db.driver";
    private static final String PROP_URL      = "db.url";
    private static final String PROP_USER     = "db.user";
    private static final String PROP_PASSWORD = "db.password";

    // -----------------------------------------------------------------
    // Loaded properties (populated once, thread-safely)
    // -----------------------------------------------------------------

    private static final Properties DB_PROPS = new Properties();
    private static       Exception  initError = null;  // non-null → init failed

    static {
        loadProperties();
    }

    // Private constructor – utility class, never instantiated.
    private DatabaseConnection() {}

    // -----------------------------------------------------------------
    // Public API
    // -----------------------------------------------------------------

    /**
     * Opens and returns a new {@link Connection} to the expense_tracker
     * database.
     *
     * <p>The caller is responsible for closing the connection; using a
     * <em>try-with-resources</em> block is strongly recommended:</p>
     * <pre>{@code
     * try (Connection conn = DatabaseConnection.getConnection()) {
     *     …
     * }
     * }</pre>
     *
     * @return a live {@code Connection}
     * @throws SQLException if the driver cannot connect or if the
     *                      properties file failed to load during
     *                      static initialisation
     */
    public static Connection getConnection() throws SQLException {
        if (initError != null) {
            throw new SQLException(
                    "DatabaseConnection failed to initialise – check config/db.properties.",
                    initError);
        }

        String url      = DB_PROPS.getProperty(PROP_URL);
        String user     = DB_PROPS.getProperty(PROP_USER);
        String password = DB_PROPS.getProperty(PROP_PASSWORD);

        // DriverManager.getConnection() is thread-safe; each call yields
        // an independent physical connection.
        return DriverManager.getConnection(url, user, password);
    }

    /**
     * Tests that a connection can be established. Prints a success or
     * failure message to {@code System.out}. Intended for use during
     * development / CI verification only – do not call from UI code.
     */
    public static void testConnection() {
        System.out.println("──────────────────────────────────────────");
        System.out.println(" ExpenseTracker – DB Connection Test");
        System.out.println("──────────────────────────────────────────");
        System.out.println(" Config path : " + resolvedConfigPath());
        System.out.println(" URL         : " + DB_PROPS.getProperty(PROP_URL, "<not loaded>"));
        System.out.println(" User        : " + DB_PROPS.getProperty(PROP_USER, "<not loaded>"));

        if (initError != null) {
            System.err.println("[FAIL] Initialisation error: " + initError.getMessage());
            return;
        }

        try (Connection conn = getConnection()) {
            System.out.println("[OK]  Connected successfully!");
            System.out.println("      Product  : " + conn.getMetaData().getDatabaseProductName());
            System.out.println("      Version  : " + conn.getMetaData().getDatabaseProductVersion());
            System.out.println("      Catalog  : " + conn.getCatalog());
        } catch (SQLException e) {
            System.err.println("[FAIL] " + e.getMessage());
            System.err.println("       SQLState : " + e.getSQLState());
            System.err.println("       Code     : " + e.getErrorCode());
        }
        System.out.println("──────────────────────────────────────────");
    }

    // -----------------------------------------------------------------
    // Internal helpers
    // -----------------------------------------------------------------

    /** Loads {@code db.properties}; stores any error in {@code initError}. */
    private static synchronized void loadProperties() {
        String configPath = resolvedConfigPath();

        // 1. Try as a filesystem path (relative to working directory).
        try (InputStream fis = new FileInputStream(configPath)) {
            DB_PROPS.load(fis);
            registerDriver();
            return;
        } catch (IOException ignored) {
            // fall through to classpath attempt
        } catch (ClassNotFoundException e) {
            initError = e;
            return;
        }

        // 2. Fall back to classpath resource (useful when running from a JAR).
        try (InputStream cpStream =
                     DatabaseConnection.class.getClassLoader()
                                              .getResourceAsStream(configPath)) {
            if (cpStream == null) {
                initError = new IOException(
                        "db.properties not found at '" + configPath +
                        "' (filesystem) or on the classpath.");
                return;
            }
            DB_PROPS.load(cpStream);
            registerDriver();
        } catch (IOException | ClassNotFoundException e) {
            initError = e;
        }
    }

    /**
     * Explicitly loads the JDBC driver class so that older environments
     * (pre-JDBC 4 service-loader) still work.
     * Modern JDBC 4+ drivers self-register, so this is a safety net only.
     */
    private static void registerDriver() throws ClassNotFoundException {
        String driver = DB_PROPS.getProperty(PROP_DRIVER,
                "com.mysql.cj.jdbc.Driver");
        Class.forName(driver);
    }

    /** Resolves the config path, honouring the system property override. */
    private static String resolvedConfigPath() {
        return System.getProperty("db.config.path", DEFAULT_CONFIG_PATH);
    }
}
