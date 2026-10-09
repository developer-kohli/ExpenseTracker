import ui.LoginFrame;

import javax.swing.*;

/**
 * Entry point for the ExpenseTracker desktop application.
 *
 * <p>Responsibilities:
 * <ul>
 *   <li>Bootstrap the Swing Event Dispatch Thread (EDT)</li>
 *   <li>Apply the system Look-and-Feel</li>
 *   <li>Launch the initial Login window</li>
 * </ul>
 *
 * <p>Architecture overview:
 * <pre>
 * src/
 * ├── model/    – Plain domain objects (User, Expense, Category …)
 * ├── dao/      – JDBC data-access objects (interfaces + MySQL implementations)
 * ├── service/  – Business logic; coordinates DAOs, runs SwingWorker tasks
 * ├── ui/       – Swing frames, panels and dialogs
 * ├── util/     – Shared helpers (DB connection, validators, formatters …)
 * └── Main.java – Application entry point (this file)
 * </pre>
 */
public class Main {

    public static void main(String[] args) {
        // All Swing UI work must happen on the Event Dispatch Thread.
        SwingUtilities.invokeLater(Main::launch);
    }

    /** Applies L&F and opens the Login window. */
    private static void launch() {
        applyLookAndFeel();
        new LoginFrame().setVisible(true);
    }

    /**
     * Attempts to apply the system native Look-and-Feel.
     * Falls back silently to the default cross-platform L&F on failure.
     */
    private static void applyLookAndFeel() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            // Non-fatal: Swing will use its default cross-platform L&F.
            System.err.println("[WARN] Could not apply system L&F: " + e.getMessage());
        }
    }
}
