package dao;

import model.Expense;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * MySQL / JDBC implementation of {@link ExpenseDAO}.
 *
 * <h3>OOP roles demonstrated here</h3>
 * <ul>
 *   <li><b>Interface implementation</b> – {@code implements ExpenseDAO}
 *       is the primary "Demonstrate interfaces" point for this project.
 *       The service layer depends only on the {@link ExpenseDAO} type;
 *       it never imports this concrete class directly.</li>
 *   <li><b>Polymorphism</b> – a variable of type {@link ExpenseDAO}
 *       holds a {@code MySQLExpenseDAO} at runtime; callers invoke
 *       interface methods without knowing the concrete type.</li>
 * </ul>
 *
 * <h3>JDBC discipline enforced throughout</h3>
 * <ul>
 *   <li>Every parameterised query uses {@link PreparedStatement} –
 *       no string concatenation of user input anywhere.</li>
 *   <li>Every {@link Connection}, {@link PreparedStatement} and
 *       {@link ResultSet} is opened inside a <em>try-with-resources</em>
 *       block so it is closed automatically on success <em>and</em>
 *       on exception.</li>
 *   <li>All queries include {@code AND user_id = ?} where applicable,
 *       ensuring strict per-user data isolation.</li>
 * </ul>
 *
 * <h3>Date conversion</h3>
 * <p>{@code LocalDate → SQL Date}: {@code Date.valueOf(localDate)}<br>
 * {@code SQL Date → LocalDate}: {@code sqlDate.toLocalDate()}
 *
 * <h3>No Swing code in this class</h3>
 * <p>All database work runs on whatever thread the caller chooses
 * (typically a {@code SwingWorker} background thread in the service
 * layer).  This class has zero knowledge of the UI.
 */
// Polymorphism: Concrete JDBC implementation of ExpenseDAO
public class MySQLExpenseDAO implements ExpenseDAO {

    // ----------------------------------------------------------------
    // SQL constants  – all queries use ? placeholders, never concatenation
    // ----------------------------------------------------------------

    private static final String SQL_INSERT =
            "INSERT INTO expenses (user_id, amount, category, expense_date, description) " +
            "VALUES (?, ?, ?, ?, ?)";

    private static final String SQL_UPDATE =
            "UPDATE expenses " +
            "SET amount = ?, category = ?, expense_date = ?, description = ? " +
            "WHERE id = ? AND user_id = ?";           // user_id scope – security

    private static final String SQL_DELETE =
            "DELETE FROM expenses " +
            "WHERE id = ? AND user_id = ?";           // user_id scope – security

    private static final String SQL_SELECT_BY_ID =
            "SELECT id, user_id, amount, category, expense_date, description " +
            "FROM expenses " +
            "WHERE id = ? AND user_id = ?";           // user_id scope – security

    private static final String SQL_SELECT_ALL =
            "SELECT id, user_id, amount, category, expense_date, description " +
            "FROM expenses " +
            "WHERE user_id = ? " +
            "ORDER BY expense_date DESC, id DESC";

    private static final String SQL_TOTAL =
            "SELECT COALESCE(SUM(amount), 0) AS total " +
            "FROM expenses " +
            "WHERE user_id = ?";

    private static final String SQL_SELECT_BY_CATEGORY =
            "SELECT id, user_id, amount, category, expense_date, description " +
            "FROM expenses " +
            "WHERE user_id = ? AND category = ? " +
            "ORDER BY expense_date DESC, id DESC";

    private static final String SQL_SELECT_BY_DATE_RANGE =
            "SELECT id, user_id, amount, category, expense_date, description " +
            "FROM expenses " +
            "WHERE user_id = ? " +
            "  AND expense_date BETWEEN ? AND ? " +   // inclusive on both ends
            "ORDER BY expense_date ASC, id ASC";

    // ================================================================
    // Interface implementation
    // ================================================================

    /**
     * {@inheritDoc}
     *
     * <p>Uses {@link Statement#RETURN_GENERATED_KEYS} to retrieve the
     * auto-incremented primary key produced by MySQL and writes it back
     * into {@code expense} via {@link model.BaseEntity#setId(int)}.
     */
    @Override
    public boolean addExpense(Expense expense) throws SQLException {
        // try-with-resources: Connection and PreparedStatement are
        // both closed automatically when the block exits.
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     SQL_INSERT, Statement.RETURN_GENERATED_KEYS)) {

            bindInsertParams(ps, expense);
            int rowsAffected = ps.executeUpdate();

            if (rowsAffected == 1) {
                // Retrieve and store the DB-generated primary key.
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) {
                        expense.setId(keys.getInt(1)); // write-back
                    }
                }
                return true;
            }
            return false;
        }
    }

    /**
     * {@inheritDoc}
     *
     * <p>The WHERE clause includes {@code AND user_id = ?} so that a user
     * cannot update another user's expense even if they somehow know the
     * target {@code id}.
     */
    @Override
    public boolean updateExpense(Expense expense) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_UPDATE)) {

            // SET params
            ps.setBigDecimal(1, expense.getAmount());
            ps.setString   (2, expense.getCategory());
            ps.setDate     (3, Date.valueOf(expense.getExpenseDate())); // LocalDate → SQL Date
            setNullableString(ps, 4, expense.getDescription());

            // WHERE params
            ps.setInt(5, expense.getId());
            ps.setInt(6, expense.getUserId());  // security scope

            return ps.executeUpdate() == 1;
        }
    }

    /**
     * {@inheritDoc}
     *
     * <p>The WHERE clause includes both {@code id} and {@code user_id}
     * so that a user can only ever delete their own records.
     */
    @Override
    public boolean deleteExpense(int expenseId, int userId) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_DELETE)) {

            ps.setInt(1, expenseId);
            ps.setInt(2, userId);   // security scope

            return ps.executeUpdate() == 1;
        }
    }

    /**
     * {@inheritDoc}
     *
     * <p>Returns {@link Optional#empty()} rather than {@code null} when
     * no matching row is found, eliminating the risk of
     * {@code NullPointerException} in the caller.
     */
    @Override
    public Optional<Expense> getExpenseById(int expenseId, int userId) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_SELECT_BY_ID)) {

            ps.setInt(1, expenseId);
            ps.setInt(2, userId);   // security scope

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        }
        return Optional.empty();
    }

    /**
     * {@inheritDoc}
     *
     * <p>Results are ordered newest-first ({@code expense_date DESC})
     * for display in the expense-history table.
     */
    @Override
    public List<Expense> getAllExpenses(int userId) throws SQLException {
        List<Expense> expenses = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_SELECT_ALL)) {

            ps.setInt(1, userId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    expenses.add(mapRow(rs));
                }
            }
        }
        return expenses;
    }

    /**
     * {@inheritDoc}
     *
     * <p>Uses {@code COALESCE(SUM(amount), 0)} so that a user with no
     * expenses still receives {@link BigDecimal#ZERO} rather than a SQL
     * {@code NULL} that would require a null-check in the caller.
     */
    @Override
    public BigDecimal calculateTotalExpenses(int userId) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_TOTAL)) {

            ps.setInt(1, userId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    BigDecimal total = rs.getBigDecimal("total");
                    return (total != null) ? total : BigDecimal.ZERO;
                }
            }
        }
        return BigDecimal.ZERO;
    }

    /**
     * {@inheritDoc}
     *
     * <p>Category matching is case-sensitive (MySQL {@code VARCHAR}
     * with {@code utf8mb4_unicode_ci} collation is case-insensitive
     * by default, so "food" and "Food" will both match).
     */
    @Override
    public List<Expense> getExpensesByCategory(int userId, String category)
            throws SQLException {

        List<Expense> expenses = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_SELECT_BY_CATEGORY)) {

            ps.setInt   (1, userId);
            ps.setString(2, category);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    expenses.add(mapRow(rs));
                }
            }
        }
        return expenses;
    }

    /**
     * {@inheritDoc}
     *
     * <p>The SQL {@code BETWEEN ? AND ?} operator is inclusive on both
     * endpoints, matching the intent of "from startDate to endDate".
     * Results are ordered oldest-first for chronological display.
     */
    @Override
    public List<Expense> getExpensesByDateRange(int userId, LocalDate startDate,
                                                 LocalDate endDate) throws SQLException {
        List<Expense> expenses = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_SELECT_BY_DATE_RANGE)) {

            ps.setInt (1, userId);
            ps.setDate(2, Date.valueOf(startDate)); // LocalDate → SQL Date
            ps.setDate(3, Date.valueOf(endDate));   // LocalDate → SQL Date

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    expenses.add(mapRow(rs));
                }
            }
        }
        return expenses;
    }

    // ================================================================
    // Private helpers
    // ================================================================

    /**
     * Maps one row from a {@link ResultSet} to an {@link Expense} object.
     *
     * <p>Date conversion: {@code rs.getDate("expense_date").toLocalDate()}
     * converts the SQL {@code DATE} column directly to a Java
     * {@link LocalDate} — no {@code SimpleDateFormat} required.
     *
     * <p>This method is the single place in this class where column
     * names are referenced, making schema refactors easy to track.
     *
     * @param rs a {@link ResultSet} positioned at a valid row
     * @return a fully populated {@link Expense}
     * @throws SQLException if a column cannot be read
     */
    private Expense mapRow(ResultSet rs) throws SQLException {
        int       id          = rs.getInt("id");
        int       userId      = rs.getInt("user_id");
        BigDecimal amount     = rs.getBigDecimal("amount");
        String    category    = rs.getString("category");
        // SQL Date → LocalDate: the canonical conversion via JDBC 4.2+
        LocalDate expenseDate = rs.getDate("expense_date").toLocalDate();
        String    description = rs.getString("description"); // may be null — that is fine

        return new Expense(id, userId, amount, category, expenseDate, description);
    }

    /**
     * Binds the fields of {@code expense} to the INSERT
     * {@link PreparedStatement} parameter positions.
     *
     * @param ps      prepared INSERT statement
     * @param expense the expense whose data is to be bound
     * @throws SQLException if a parameter cannot be set
     */
    private void bindInsertParams(PreparedStatement ps, Expense expense)
            throws SQLException {
        ps.setInt       (1, expense.getUserId());
        ps.setBigDecimal(2, expense.getAmount());
        ps.setString    (3, expense.getCategory());
        ps.setDate      (4, Date.valueOf(expense.getExpenseDate())); // LocalDate → SQL Date
        setNullableString(ps, 5, expense.getDescription());
    }

    /**
     * Sets a {@link String} parameter, using SQL {@code NULL} when the
     * value is Java {@code null}.  This handles the optional
     * {@code description} column cleanly.
     *
     * @param ps       the prepared statement
     * @param index    1-based parameter index
     * @param value    the string value, or {@code null}
     * @throws SQLException if the parameter cannot be set
     */
    private void setNullableString(PreparedStatement ps, int index, String value)
            throws SQLException {
        if (value != null) {
            ps.setString(index, value);
        } else {
            ps.setNull(index, Types.VARCHAR);
        }
    }
}
