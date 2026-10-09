package model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Domain object representing a single expense recorded by a user.
 *
 * <h3>OOP notes</h3>
 * <ul>
 *   <li><b>Inheritance</b>: extends {@link BaseEntity} to inherit the
 *       surrogate {@code id} field and its accessors.</li>
 *   <li><b>Encapsulation</b>: all fields are {@code private}; state is
 *       exposed only through typed getters/setters.</li>
 *   <li><b>No JDBC</b>: persistence is handled entirely by
 *       {@code dao.ExpenseDAO}.</li>
 * </ul>
 *
 * <h3>Why {@code BigDecimal} for amount?</h3>
 * <p>Floating-point types ({@code float}, {@code double}) cannot
 * represent many decimal fractions exactly, which leads to rounding
 * errors in financial calculations (e.g. 0.1 + 0.2 ≠ 0.3 in IEEE 754).
 * {@code BigDecimal} avoids this completely and maps directly to
 * MySQL's {@code DECIMAL(10,2)} column type.
 *
 * <h3>Why {@code LocalDate} for expenseDate?</h3>
 * <p>{@code LocalDate} is timezone-agnostic, immutable, and maps
 * cleanly to MySQL's {@code DATE} type via
 * {@code PreparedStatement.setDate()} with
 * {@code Date.valueOf(localDate)}.
 */
// Inheritance: Expense extends abstract BaseEntity
public class Expense extends BaseEntity {

    // ----------------------------------------------------------------
    // Fields (Encapsulation: private fields accessed via getters/setters)
    // ----------------------------------------------------------------

    // Encapsulation
    private int    userId;
    private BigDecimal amount;
    private String category;
    private LocalDate expenseDate;
    private String description;

    // ----------------------------------------------------------------
    // Formatter shared by toString()
    // ----------------------------------------------------------------

    private static final DateTimeFormatter DATE_FMT =
            DateTimeFormatter.ofPattern("dd-MMM-yyyy");

    // ----------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------

    /**
     * No-arg constructor.
     * Required for reflection-based frameworks and result-set mapping.
     */
    public Expense() {
        super();
    }

    /**
     * Validation-friendly constructor – used when <em>adding</em> a new
     * expense before the database assigns an ID.
     *
     * <p>Sets {@code description} to {@code null} (optional field).
     * The {@code id} is left as {@code 0} and is populated by the DAO
     * after a successful {@code INSERT}.
     *
     * @param userId      ID of the owning user (must match an existing {@code users.id})
     * @param amount      positive monetary amount
     * @param category    non-blank category label
     * @param expenseDate the date the expense occurred (non-null)
     */
    public Expense(int userId, BigDecimal amount, String category, LocalDate expenseDate) {
        this(userId, amount, category, expenseDate, null);
    }

    /**
     * Validation-friendly constructor with optional description – used
     * when <em>adding</em> a new expense before the database assigns an ID.
     *
     * @param userId      ID of the owning user
     * @param amount      positive monetary amount
     * @param category    non-blank category label
     * @param expenseDate the date the expense occurred (non-null)
     * @param description optional free-text note (may be {@code null})
     */
    public Expense(int userId, BigDecimal amount, String category,
                   LocalDate expenseDate, String description) {
        super();
        this.userId      = userId;
        this.amount      = amount;
        this.category    = category;
        this.expenseDate = expenseDate;
        this.description = description;
    }

    /**
     * Full constructor – used when <em>loading</em> an existing expense
     * from the database (id is already known).
     *
     * @param id          database primary key
     * @param userId      owning user's ID
     * @param amount      monetary amount
     * @param category    category label
     * @param expenseDate date of the expense
     * @param description optional note (may be {@code null})
     */
    public Expense(int id, int userId, BigDecimal amount, String category,
                   LocalDate expenseDate, String description) {
        super(id);
        this.userId      = userId;
        this.amount      = amount;
        this.category    = category;
        this.expenseDate = expenseDate;
        this.description = description;
    }

    // ----------------------------------------------------------------
    // Getters & Setters
    // ----------------------------------------------------------------

    /**
     * @return the foreign key that links this expense to its owning user
     */
    public int getUserId() { return userId; }

    /**
     * @param userId the owning user's primary key
     */
    public void setUserId(int userId) { this.userId = userId; }

    /**
     * @return the expense amount as an exact-precision {@link BigDecimal}
     */
    public BigDecimal getAmount() { return amount; }

    /**
     * @param amount positive monetary amount (scale 2 recommended,
     *               e.g. {@code new BigDecimal("12.50")})
     */
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    /**
     * @return the category label (e.g. "Food", "Transport")
     */
    public String getCategory() { return category; }

    /**
     * @param category non-blank category string
     */
    public void setCategory(String category) { this.category = category; }

    /**
     * @return the date on which the expense occurred
     */
    public LocalDate getExpenseDate() { return expenseDate; }

    /**
     * @param expenseDate non-null calendar date of the expense
     */
    public void setExpenseDate(LocalDate expenseDate) { this.expenseDate = expenseDate; }

    /**
     * @return an optional free-text description, or {@code null} if none
     */
    public String getDescription() { return description; }

    /**
     * @param description optional note; pass {@code null} to clear
     */
    public void setDescription(String description) { this.description = description; }

    // ----------------------------------------------------------------
    // Object overrides
    // ----------------------------------------------------------------

    /**
     * Human-readable summary of the expense, useful for logging and
     * debugging.
     *
     * <p>Example output:
     * <pre>
     * Expense{id=7, userId=1, amount=12.50, category='Food',
     *         date=01-Oct-2026, description='Lunch at office'}
     * </pre>
     *
     * @return formatted string representation
     */
    @Override
    public String toString() {
        return "Expense{" +
               "id="          + getId()  +
               ", userId="    + userId   +
               ", amount="    + (amount != null ? amount.toPlainString() : "null") +
               ", category='" + category + '\'' +
               ", date="      + (expenseDate != null ? expenseDate.format(DATE_FMT) : "null") +
               ", description='" + (description != null ? description : "") + '\'' +
               '}';
    }
}
