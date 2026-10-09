package util;

import dao.DatabaseConnection;

/**
 * Standalone connection-test runner.
 *
 * <p>Run this class (not {@code Main}) to verify that JDBC connectivity
 * is working before launching the full application.
 *
 * <h3>How to compile &amp; run</h3>
 * <pre>
 * # From the project root (d:\ExpenseTracker)
 *
 * # 1. Compile
 * javac -cp "lib\mysql-connector-j-8.3.0.jar" -d out (Get-ChildItem -Recurse -Filter "*.java" src | % FullName)
 *
 * # 2. Run the test
 * java -cp "out;lib\mysql-connector-j-8.3.0.jar" util.DBConnectionTest
 * </pre>
 *
 * <p>Expected output on success:
 * <pre>
 * ──────────────────────────────────────────
 *  ExpenseTracker – DB Connection Test
 * ──────────────────────────────────────────
 *  Config path : config/db.properties
 *  URL         : jdbc:mysql://localhost:3306/expense_tracker?...
 *  User        : root
 * [OK]  Connected successfully!
 *       Product  : MySQL
 *       Version  : 8.x.x
 *       Catalog  : expense_tracker
 * ──────────────────────────────────────────
 * </pre>
 */
public class DBConnectionTest {

    public static void main(String[] args) {
        DatabaseConnection.testConnection();
    }
}
