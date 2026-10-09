package service;

import dao.ExpenseDAO;
import dao.MySQLExpenseDAO;
import model.Expense;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Concrete implementation of {@link ExpenseService}.
 *
 * <h3>OOP roles demonstrated here</h3>
 * <ul>
 *   <li><b>Interface implementation</b> – {@code implements ExpenseService};
 *       SwingWorker tasks reference only the {@link ExpenseService} type.</li>
 *   <li><b>Polymorphism</b> – an {@code ExpenseService} variable holds
 *       this instance at runtime; a different implementation could be
 *       substituted without changing any UI code.</li>
 *   <li><b>Dependency injection via constructor</b> – the {@link ExpenseDAO}
 *       is injected so it can be swapped for a test stub.</li>
 * </ul>
 *
 * <h3>Business rules enforced here</h3>
 * <ul>
 *   <li>Amount must be positive (greater than zero).</li>
 *   <li>Category must not be blank.</li>
 *   <li>Expense date must not be null.</li>
 *   <li>Date range: {@code startDate} must not be after {@code endDate}.</li>
 *   <li>User ownership: {@code userId} must be positive and is always
 *       forwarded to the DAO so the DB enforces the {@code AND user_id = ?}
 *       constraint.</li>
 * </ul>
 *
 * <h3>No Swing code in this class</h3>
 * <p>Zero imports from {@code javax.swing} or {@code java.awt}.
 * SwingWorker background threads call these methods safely because all
 * operations are stateless (no shared mutable state).
 */
// Polymorphism: Concrete implementation of the ExpenseService interface
public class ExpenseServiceImpl implements ExpenseService {

    // ----------------------------------------------------------------
    // DAO dependency (injected – never instantiated inline)
    // ----------------------------------------------------------------

    // Encapsulation: DAO reference held as private final field
    private final ExpenseDAO expenseDAO;

    // ----------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------

    /**
     * Default constructor – creates a {@link MySQLExpenseDAO} internally.
     * Convenient for production use.
     */
    public ExpenseServiceImpl() {
        this(new MySQLExpenseDAO());
    }

    /**
     * Dependency-injection constructor.
     * Use this constructor in tests to inject a stub/mock DAO.
     *
     * @param expenseDAO the DAO implementation to delegate persistence to
     */
    public ExpenseServiceImpl(ExpenseDAO expenseDAO) {
        this.expenseDAO = expenseDAO;
    }

    // ================================================================
    // ExpenseService implementation
    // ================================================================

    /**
     * {@inheritDoc}
     *
     * <p><b>Business rules</b>: amount &gt; 0, category non-blank,
     * date non-null.
     */
    @Override
    public boolean addExpense(Expense expense) throws ServiceException {
        validateExpense(expense);
        try {
            return expenseDAO.addExpense(expense);
        } catch (SQLException e) {
            throw new ServiceException(
                    "Could not save the expense. Please try again.", e);
        }
    }

    /**
     * {@inheritDoc}
     *
     * <p><b>Business rules</b>: same as {@link #addExpense}, plus
     * the expense {@code id} must be a positive value (i.e. the record
     * must already exist in the database).
     */
    @Override
    public boolean updateExpense(Expense expense) throws ServiceException {
        if (expense == null || expense.getId() <= 0) {
            throw new ServiceException("Cannot update an expense without a valid ID.");
        }
        validateExpense(expense);
        try {
            boolean updated = expenseDAO.updateExpense(expense);
            if (!updated) {
                throw new ServiceException(
                        "Expense not found or you do not have permission to edit it.");
            }
            return true;
        } catch (SQLException e) {
            throw new ServiceException(
                    "Could not update the expense. Please try again.", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean deleteExpense(int expenseId, int userId) throws ServiceException {
        validatePositiveId(expenseId, "Expense ID");
        validatePositiveId(userId,    "User ID");
        try {
            boolean deleted = expenseDAO.deleteExpense(expenseId, userId);
            if (!deleted) {
                throw new ServiceException(
                        "Expense not found or you do not have permission to delete it.");
            }
            return true;
        } catch (SQLException e) {
            throw new ServiceException(
                    "Could not delete the expense. Please try again.", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<Expense> getExpenseById(int expenseId, int userId)
            throws ServiceException {
        validatePositiveId(expenseId, "Expense ID");
        validatePositiveId(userId,    "User ID");
        try {
            return expenseDAO.getExpenseById(expenseId, userId);
        } catch (SQLException e) {
            throw new ServiceException(
                    "Could not retrieve the expense. Please try again.", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Expense> getAllExpenses(int userId) throws ServiceException {
        validatePositiveId(userId, "User ID");
        try {
            return expenseDAO.getAllExpenses(userId);
        } catch (SQLException e) {
            throw new ServiceException(
                    "Could not load expenses. Please try again.", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BigDecimal calculateTotalExpenses(int userId) throws ServiceException {
        validatePositiveId(userId, "User ID");
        try {
            return expenseDAO.calculateTotalExpenses(userId);
        } catch (SQLException e) {
            throw new ServiceException(
                    "Could not calculate total expenses. Please try again.", e);
        }
    }

    /**
     * {@inheritDoc}
     *
     * <p><b>Business rule</b>: {@code category} must not be blank.
     */
    @Override
    public List<Expense> getExpensesByCategory(int userId, String category)
            throws ServiceException {
        validatePositiveId(userId, "User ID");
        if (category == null || category.isBlank()) {
            throw new ServiceException("Category must not be empty.");
        }
        try {
            return expenseDAO.getExpensesByCategory(userId, category.trim());
        } catch (SQLException e) {
            throw new ServiceException(
                    "Could not filter expenses by category. Please try again.", e);
        }
    }

    /**
     * {@inheritDoc}
     *
     * <p><b>Business rule</b>: both dates must be non-null and
     * {@code startDate} must not be after {@code endDate}.
     */
    @Override
    public List<Expense> getExpensesByDateRange(int userId, LocalDate startDate,
                                                 LocalDate endDate)
            throws ServiceException {
        validatePositiveId(userId, "User ID");
        if (startDate == null) {
            throw new ServiceException("Start date must not be empty.");
        }
        if (endDate == null) {
            throw new ServiceException("End date must not be empty.");
        }
        if (startDate.isAfter(endDate)) {
            throw new ServiceException(
                    "Start date (" + startDate + ") must not be after "
                    + "end date (" + endDate + ").");
        }
        try {
            return expenseDAO.getExpensesByDateRange(userId, startDate, endDate);
        } catch (SQLException e) {
            throw new ServiceException(
                    "Could not filter expenses by date range. Please try again.", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BigDecimal calculateCurrentMonthExpenses(int userId) throws ServiceException {
        validatePositiveId(userId, "User ID");
        LocalDate now = LocalDate.now();
        LocalDate startOfMonth = now.withDayOfMonth(1);
        LocalDate endOfMonth = now.withDayOfMonth(now.lengthOfMonth());

        try {
            List<Expense> monthExpenses = expenseDAO.getExpensesByDateRange(userId, startOfMonth, endOfMonth);
            if (monthExpenses == null || monthExpenses.isEmpty()) {
                return BigDecimal.ZERO;
            }
            return monthExpenses.stream()
                    .map(Expense::getAmount)
                    .filter(amt -> amt != null)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
        } catch (SQLException e) {
            throw new ServiceException("Could not calculate current month expenses. Please try again.", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getExpenseCount(int userId) throws ServiceException {
        validatePositiveId(userId, "User ID");
        try {
            List<Expense> expenses = expenseDAO.getAllExpenses(userId);
            return expenses != null ? expenses.size() : 0;
        } catch (SQLException e) {
            throw new ServiceException("Could not calculate expense count. Please try again.", e);
        }
    }

    // ================================================================
    // Private validation helpers
    // ================================================================

    /**
     * Central expense validator called before every write operation.
     * Checks that the mandatory fields are present and meaningful.
     *
     * @param expense the expense to validate (may be {@code null})
     * @throws ServiceException describing the first rule violated
     */
    private void validateExpense(Expense expense) throws ServiceException {
        if (expense == null) {
            throw new ServiceException("Expense data must not be null.");
        }
        if (expense.getUserId() <= 0) {
            throw new ServiceException("Expense must be linked to a valid user.");
        }
        if (expense.getAmount() == null) {
            throw new ServiceException("Amount must not be empty.");
        }
        if (expense.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new ServiceException("Amount must be greater than zero.");
        }
        if (expense.getAmount().precision() - expense.getAmount().scale() > 8) {
            // DECIMAL(10,2) allows up to 8 digits before the decimal point
            throw new ServiceException("Amount exceeds the maximum allowed value.");
        }
        if (expense.getCategory() == null || expense.getCategory().isBlank()) {
            throw new ServiceException("Category must not be empty.");
        }
        if (expense.getCategory().length() > 50) {
            throw new ServiceException("Category must not exceed 50 characters.");
        }
        if (expense.getExpenseDate() == null) {
            throw new ServiceException("Expense date must not be empty.");
        }
        if (expense.getDescription() != null && expense.getDescription().length() > 255) {
            throw new ServiceException("Description must not exceed 255 characters.");
        }
    }

    /**
     * Validates that an ID parameter is a positive integer.
     *
     * @param id        the value to check
     * @param fieldName label used in the error message (e.g. {@code "User ID"})
     * @throws ServiceException if {@code id} is not positive
     */
    private void validatePositiveId(int id, String fieldName) throws ServiceException {
        if (id <= 0) {
            throw new ServiceException("Invalid " + fieldName + ": must be greater than 0.");
        }
    }
}
