package service;

import model.Expense;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Business-logic contract for expense management in ExpenseTracker.
 *
 * <h3>OOP role – Interfaces (demonstrated here)</h3>
 * <p>SwingWorker tasks and Swing panels depend only on this interface
 * type, never on {@link ExpenseServiceImpl}.  This makes the UI
 * independently testable and allows the backend to evolve freely.
 *
 * <h3>Layer responsibility</h3>
 * <pre>
 *   UI / SwingWorker  →  ExpenseService  →  ExpenseDAO  →  MySQL
 * </pre>
 * The service layer owns:
 * <ul>
 *   <li>Business-rule validation (positive amount, non-blank category,
 *       non-null date, user ownership)</li>
 *   <li>Translation of {@link java.sql.SQLException} →
 *       {@link ServiceException}</li>
 * </ul>
 * It does <b>not</b> own SQL queries, JDBC connections, or UI widgets.
 */
// Interface: Business-logic contract for expense management
// Abstraction: Shields UI layer from validation logic, transaction handling, and database exceptions
public interface ExpenseService {

    /**
     * Validates and persists a new expense.
     *
     * @param expense the expense to add ({@code id} must be 0 / unset)
     * @return {@code true} if the expense was saved successfully
     * @throws ServiceException if validation fails or a database error occurs
     */
    boolean addExpense(Expense expense) throws ServiceException;

    /**
     * Validates and updates an existing expense.
     *
     * @param expense the expense with updated values
     *                ({@code id} and {@code userId} must be set)
     * @return {@code true} if the expense was updated successfully
     * @throws ServiceException if validation fails or a database error occurs
     */
    boolean updateExpense(Expense expense) throws ServiceException;

    /**
     * Deletes the expense identified by {@code expenseId}, scoped to
     * {@code userId} so users can only delete their own records.
     *
     * @param expenseId the primary key of the expense to remove
     * @param userId    the owning user's ID (security scope)
     * @return {@code true} if the expense was deleted successfully
     * @throws ServiceException if a database error occurs
     */
    boolean deleteExpense(int expenseId, int userId) throws ServiceException;

    /**
     * Retrieves a single expense by ID, scoped to the given user.
     *
     * @param expenseId the expense primary key
     * @param userId    the owning user's ID (security scope)
     * @return an {@link Optional} with the expense, or empty if not found
     * @throws ServiceException if a database error occurs
     */
    Optional<Expense> getExpenseById(int expenseId, int userId) throws ServiceException;

    /**
     * Returns all expenses for a user, newest first.
     *
     * @param userId the owning user's ID
     * @return list of expenses; empty list if none exist
     * @throws ServiceException if a database error occurs
     */
    List<Expense> getAllExpenses(int userId) throws ServiceException;

    /**
     * Calculates the total amount spent by a user across all categories.
     *
     * @param userId the owning user's ID
     * @return exact total as {@link BigDecimal};
     *         {@link BigDecimal#ZERO} if no expenses exist
     * @throws ServiceException if a database error occurs
     */
    BigDecimal calculateTotalExpenses(int userId) throws ServiceException;

    /**
     * Returns all expenses for a user in a specific category.
     *
     * @param userId   the owning user's ID
     * @param category the category label to filter by
     * @return matching expenses, newest first; empty list if none
     * @throws ServiceException if validation fails or a database error occurs
     */
    List<Expense> getExpensesByCategory(int userId, String category)
            throws ServiceException;

    /**
     * Returns all expenses for a user within an inclusive date range.
     *
     * @param userId    the owning user's ID
     * @param startDate range start (inclusive)
     * @param endDate   range end (inclusive); must not be before {@code startDate}
     * @return matching expenses ordered oldest-first; empty list if none
     * @throws ServiceException if validation fails or a database error occurs
     */
    List<Expense> getExpensesByDateRange(int userId, LocalDate startDate,
                                          LocalDate endDate) throws ServiceException;

    /**
     * Calculates total expenses recorded in the current calendar month for the given user.
     *
     * @param userId the owning user's ID
     * @return monthly total as {@link BigDecimal}
     * @throws ServiceException if a database error occurs
     */
    BigDecimal calculateCurrentMonthExpenses(int userId) throws ServiceException;

    /**
     * Returns the total count of expenses recorded for the given user.
     *
     * @param userId the owning user's ID
     * @return number of recorded expenses
     * @throws ServiceException if a database error occurs
     */
    int getExpenseCount(int userId) throws ServiceException;
}
