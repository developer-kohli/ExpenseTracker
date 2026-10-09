package service;

/**
 * Checked exception thrown by the service layer when a business-rule
 * violation or database error occurs.
 *
 * <h3>Role in the architecture</h3>
 * <p>The service layer catches all {@link java.sql.SQLException}s from
 * the DAO layer and wraps them in a {@code ServiceException} before
 * re-throwing.  This means:
 * <ul>
 *   <li>The Swing UI only ever sees {@code ServiceException} — never
 *       raw SQL error codes or MySQL-specific messages.</li>
 *   <li>The message inside {@code ServiceException} is already
 *       human-readable and safe to display in a dialog.</li>
 *   <li>The original cause is preserved via {@link #getCause()} for
 *       logging and debugging.</li>
 * </ul>
 *
 * <h3>Usage</h3>
 * <pre>{@code
 * // In a service implementation:
 * try {
 *     expenseDAO.addExpense(expense);
 * } catch (SQLException e) {
 *     throw new ServiceException("Could not save expense. Please try again.", e);
 * }
 *
 * // In the UI / SwingWorker:
 * catch (ServiceException e) {
 *     JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
 * }
 * }</pre>
 */
public class ServiceException extends Exception {

    private static final long serialVersionUID = 1L;

    /**
     * Constructs a {@code ServiceException} with a user-friendly message
     * and the underlying cause.
     *
     * @param message a human-readable explanation safe for display in the UI
     * @param cause   the original exception (preserved for logging)
     */
    public ServiceException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs a {@code ServiceException} with only a user-friendly
     * message (no underlying cause).  Use for pure validation failures
     * that do not originate from a lower-level exception.
     *
     * @param message a human-readable explanation safe for display in the UI
     */
    public ServiceException(String message) {
        super(message);
    }
}
