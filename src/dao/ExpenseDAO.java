package dao;

import model.Expense;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Contract for all expense persistence operations in ExpenseTracker.
 *
 * <h3>OOP role – Interfaces (demonstrated here)</h3>
 * <p>Declaring a {@code interface} here decouples the <em>what</em>
 * (the operations) from the <em>how</em> (the MySQL implementation
 * in {@link MySQLExpenseDAO}).  Benefits:
 * <ul>
 *   <li>The {@code service} layer depends only on this type, never on
 *       a concrete class — classic Dependency-Inversion Principle.</li>
 *   <li>Implementations can be swapped (e.g. SQLite for testing,
 *       MySQL for production) without touching any caller.</li>
 *   <li>SwingWorker tasks receive an {@code ExpenseDAO} reference,
 *       keeping them free of MySQL-specific imports.</li>
 * </ul>
 *
 * <h3>Security invariant</h3>
 * <p>Every method that operates on a single expense accepts both
 * {@code expenseId} <em>and</em> {@code userId}.  Implementations
 * <strong>must</strong> include {@code AND user_id = ?} in every
 * parameterised query so a user can never access another user's data.
 */
// Interface: Defines persistence contract for expense data access
// Abstraction: Hides low-level SQL and database storage details from caller layers
public interface ExpenseDAO {

    /**
     * Persists a new expense to the database.
     *
     * <p>On success the generated primary key is written back into
     * {@code expense.id} via {@link model.BaseEntity#setId(int)}.
     *
     * @param expense the expense to insert (id should be 0 / unset)
     * @return {@code true} if the row was inserted successfully
     * @throws SQLException if a database error occurs
     */
    boolean addExpense(Expense expense) throws SQLException;

    /**
     * Updates an existing expense record.
     *
     * <p>The query must include {@code AND user_id = ?} so that only
     * the owning user can modify their own expenses.
     *
     * @param expense the expense with updated field values
     *                ({@code id} and {@code userId} must both be set)
     * @return {@code true} if exactly one row was updated
     * @throws SQLException if a database error occurs
     */
    boolean updateExpense(Expense expense) throws SQLException;

    /**
     * Deletes an expense by its primary key, scoped to the given user.
     *
     * @param expenseId primary key of the expense to remove
     * @param userId    must match {@code expenses.user_id}; prevents
     *                  cross-user deletion
     * @return {@code true} if exactly one row was deleted
     * @throws SQLException if a database error occurs
     */
    boolean deleteExpense(int expenseId, int userId) throws SQLException;

    /**
     * Fetches a single expense by ID, scoped to the given user.
     *
     * @param expenseId the expense primary key
     * @param userId    the owning user's ID (security scope)
     * @return an {@link Optional} containing the expense if found,
     *         or {@link Optional#empty()} if no matching row exists
     * @throws SQLException if a database error occurs
     */
    Optional<Expense> getExpenseById(int expenseId, int userId) throws SQLException;

    /**
     * Returns all expenses belonging to a user, newest first.
     *
     * @param userId the owning user's ID
     * @return list of expenses ordered by {@code expense_date DESC};
     *         empty list if none exist
     * @throws SQLException if a database error occurs
     */
    List<Expense> getAllExpenses(int userId) throws SQLException;

    /**
     * Calculates the sum of all expense amounts for a user.
     *
     * @param userId the owning user's ID
     * @return total as an exact-precision {@link BigDecimal};
     *         {@link BigDecimal#ZERO} if the user has no expenses
     * @throws SQLException if a database error occurs
     */
    BigDecimal calculateTotalExpenses(int userId) throws SQLException;

    /**
     * Returns all expenses for a user filtered by category.
     *
     * @param userId   the owning user's ID
     * @param category category label to filter by (exact match)
     * @return matching expenses ordered by {@code expense_date DESC};
     *         empty list if none match
     * @throws SQLException if a database error occurs
     */
    List<Expense> getExpensesByCategory(int userId, String category) throws SQLException;

    /**
     * Returns expenses for a user within an inclusive date range.
     *
     * @param userId    the owning user's ID
     * @param startDate first date in the range (inclusive)
     * @param endDate   last date in the range (inclusive)
     * @return matching expenses ordered by {@code expense_date ASC};
     *         empty list if none match
     * @throws SQLException if a database error occurs
     */
    List<Expense> getExpensesByDateRange(int userId, LocalDate startDate,
                                         LocalDate endDate) throws SQLException;
}
