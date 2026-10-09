package model;

import java.math.BigDecimal;

/**
 * Represents a standard, non-administrative user of the ExpenseTracker
 * application.
 *
 * <h3>Inheritance – demonstrated here</h3>
 * <pre>
 *   BaseEntity
 *   └── User              ← parent (name, email, password, getRole())
 *       └── RegularUser   ← this class (adds monthlyBudget)
 * </pre>
 *
 * <p>RegularUser inherits all fields and behaviour from {@link User}
 * (name, email, password, getters/setters, {@code equals}/
 * {@code hashCode} from {@link BaseEntity}) without rewriting any of
 * that code.  It adds only the field that is specific to this role:
 * an optional <em>monthly budget cap</em> that the UI can use to
 * warn the user when their spending is approaching the limit.
 *
 * <h3>Method overriding – demonstrated here</h3>
 * <p>Both {@link #getRole()} and {@link #getRoleDescription()} are
 * declared in {@link User} and overridden here with
 * {@code @Override} so that a polymorphic {@code User} reference
 * returns the correct values at runtime:
 * <pre>{@code
 * User u = new RegularUser("Alice", "alice@example.com", hashedPwd, BigDecimal.ZERO);
 * System.out.println(u.getRole());            // "REGULAR_USER"
 * System.out.println(u.getRoleDescription()); // "Regular User"
 * }</pre>
 */
// Inheritance: RegularUser is a child class of User
public class RegularUser extends User {

    // ----------------------------------------------------------------
    // RegularUser-specific field (Encapsulation: private field)
    // ----------------------------------------------------------------

    // Encapsulation
    private BigDecimal monthlyBudget;

    // ----------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------

    /**
     * No-arg constructor.
     * Required for reflection-based frameworks.
     */
    public RegularUser() {
        super();
        this.monthlyBudget = BigDecimal.ZERO;
    }

    /**
     * Creation constructor – used when registering a new regular user
     * before the database assigns an ID.
     *
     * @param name          full display name (non-blank)
     * @param email         unique e-mail address (non-blank)
     * @param password      already-hashed password (non-blank)
     * @param monthlyBudget optional monthly budget cap; pass
     *                      {@code BigDecimal.ZERO} for "no limit"
     */
    public RegularUser(String name, String email, String password,
                       BigDecimal monthlyBudget) {
        // INHERITANCE: delegates core field initialisation to the
        // parent User constructor — no duplication of name/email/password
        // initialisation code.
        super(name, email, password);
        this.monthlyBudget = (monthlyBudget != null) ? monthlyBudget : BigDecimal.ZERO;
    }

    /**
     * Full constructor – used when loading an existing regular user
     * from the database (id is already known).
     *
     * @param id            database primary key
     * @param name          full display name
     * @param email         unique e-mail address
     * @param password      hashed password
     * @param monthlyBudget monthly budget cap ({@code BigDecimal.ZERO} = none)
     */
    public RegularUser(int id, String name, String email, String password,
                       BigDecimal monthlyBudget) {
        // INHERITANCE: delegates to the full User constructor (id included)
        super(id, name, email, password);
        this.monthlyBudget = (monthlyBudget != null) ? monthlyBudget : BigDecimal.ZERO;
    }

    // ----------------------------------------------------------------
    // Overridden methods  ← METHOD OVERRIDING demonstrated here
    // ----------------------------------------------------------------

    /**
     * {@inheritDoc}
     *
     * <p><b>Method Overriding</b>: overrides {@link User#getRole()} to
     * return {@code "REGULAR_USER"} instead of the generic {@code "USER"}.
     * Any code holding a polymorphic {@code User} reference will
     * receive this value at runtime when the actual object is a
     * {@code RegularUser}.
     */
    // Polymorphism: Dynamic method overriding of parent User.getRole()
    @Override
    public String getRole() {
        return "REGULAR_USER";  // overrides User.getRole() → "USER"
    }

    /**
     * {@inheritDoc}
     *
     * <p><b>Method Overriding</b>: provides a role-specific label used
     * by the UI's welcome message and the admin management panel.
     */
    @Override
    public String getRoleDescription() {
        return "Regular User";  // overrides User.getRoleDescription()
    }

    /**
     * Returns a formatted summary including the budget cap.
     * Overrides {@link User#toString()} to add the subclass-specific
     * field while reusing the parent data via {@code super} getters.
     */
    @Override
    public String toString() {
        return "RegularUser{" +
               "id="             + getId()    +
               ", name='"        + getName()  + '\'' +
               ", email='"       + getEmail() + '\'' +
               ", monthlyBudget=" + monthlyBudget.toPlainString() +
               '}';
    }

    // ----------------------------------------------------------------
    // RegularUser-specific getter / setter
    // ----------------------------------------------------------------

    /**
     * @return the monthly budget cap, or {@code BigDecimal.ZERO} if
     *         none is configured
     */
    public BigDecimal getMonthlyBudget() { return monthlyBudget; }

    /**
     * @param monthlyBudget spending cap per calendar month; use
     *                      {@code BigDecimal.ZERO} to disable the limit
     */
    public void setMonthlyBudget(BigDecimal monthlyBudget) {
        this.monthlyBudget = (monthlyBudget != null) ? monthlyBudget : BigDecimal.ZERO;
    }

    /**
     * Convenience check used by the service layer to decide whether to
     * show a budget warning on the dashboard.
     *
     * @return {@code true} if a positive monthly budget limit is set
     */
    public boolean hasBudgetLimit() {
        return monthlyBudget.compareTo(BigDecimal.ZERO) > 0;
    }
}
