package model;

/**
 * Represents an administrator of the ExpenseTracker application.
 *
 * <h3>Inheritance – demonstrated here</h3>
 * <pre>
 *   BaseEntity
 *   └── User           ← parent (name, email, password, getRole())
 *       └── AdminUser  ← this class (adds adminLevel, canManageUsers())
 * </pre>
 *
 * <p>AdminUser inherits every field and method from {@link User} without
 * duplicating them.  It adds the concept of an <em>admin level</em>
 * (1 = read-only auditor, 2 = full administrator) and exposes a set of
 * privilege-check helpers that the service / UI layers can call in a
 * uniform, polymorphic way.
 *
 * <h3>Method overriding – demonstrated here</h3>
 * <p>Both {@link #getRole()} and {@link #getRoleDescription()} are
 * declared in {@link User} with default implementations and are
 * overridden here so that a polymorphic {@code User} reference returns
 * admin-specific values:
 * <pre>{@code
 * User u = new AdminUser("Bob", "bob@example.com", hashedPwd, 2);
 * System.out.println(u.getRole());            // "ADMIN"
 * System.out.println(u.getRoleDescription()); // "Administrator (Level 2)"
 * }</pre>
 *
 * <h3>Admin levels</h3>
 * <ul>
 *   <li>{@code 1} – Auditor: can view all users' expenses (read-only)</li>
 *   <li>{@code 2} – Full Admin: can view <em>and</em> delete any user's data</li>
 * </ul>
 */
// Inheritance: AdminUser is a child class of User
public class AdminUser extends User {

    // ----------------------------------------------------------------
    // AdminUser-specific fields (Encapsulation: private field)
    // ----------------------------------------------------------------

    // Encapsulation
    private int adminLevel;

    // ----------------------------------------------------------------
    // Constants
    // ----------------------------------------------------------------

    /** Auditor: can view all expense records but cannot modify them. */
    public static final int LEVEL_AUDITOR   = 1;

    /** Full admin: unrestricted read/write access across all users. */
    public static final int LEVEL_FULL_ADMIN = 2;

    // ----------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------

    /**
     * No-arg constructor.
     * Defaults to the lowest admin level ({@code LEVEL_AUDITOR}).
     */
    public AdminUser() {
        super();
        this.adminLevel = LEVEL_AUDITOR;
    }

    /**
     * Creation constructor – used when promoting a user to admin
     * before the database assigns an ID.
     *
     * @param name       full display name (non-blank)
     * @param email      unique e-mail address (non-blank)
     * @param password   already-hashed password (non-blank)
     * @param adminLevel privilege level (use {@link #LEVEL_AUDITOR} or
     *                   {@link #LEVEL_FULL_ADMIN})
     */
    public AdminUser(String name, String email, String password, int adminLevel) {
        // INHERITANCE: delegates core field initialisation to the
        // parent User constructor — no code duplication.
        super(name, email, password);
        this.adminLevel = adminLevel;
    }

    /**
     * Full constructor – used when loading an existing admin user from
     * the database (id is already known).
     *
     * @param id         database primary key
     * @param name       full display name
     * @param email      unique e-mail address
     * @param password   hashed password
     * @param adminLevel privilege level
     */
    public AdminUser(int id, String name, String email, String password,
                     int adminLevel) {
        // INHERITANCE: delegates to the full User constructor (id included)
        super(id, name, email, password);
        this.adminLevel = adminLevel;
    }

    // ----------------------------------------------------------------
    // Overridden methods  ← METHOD OVERRIDING demonstrated here
    // ----------------------------------------------------------------

    /**
     * {@inheritDoc}
     *
     * <p><b>Method Overriding</b>: overrides {@link User#getRole()} to
     * return {@code "ADMIN"} instead of the base {@code "USER"}.
     * Service/UI code that calls {@code user.getRole()} on a
     * polymorphic {@code User} reference receives {@code "ADMIN"} when
     * the concrete object is an {@code AdminUser}.
     */
    // Polymorphism: Dynamic method overriding of parent User.getRole()
    @Override
    public String getRole() {
        return "ADMIN";   // overrides User.getRole() → "USER"
    }

    /**
     * {@inheritDoc}
     *
     * <p><b>Method Overriding</b>: includes the numeric admin level in
     * the description so that the management UI can display meaningful
     * role labels without extra switch statements.
     */
    @Override
    public String getRoleDescription() {
        // Dynamically builds the description based on this instance's
        // adminLevel — another practical use of method overriding.
        return switch (adminLevel) {
            case LEVEL_AUDITOR    -> "Auditor (Read-Only Admin)";
            case LEVEL_FULL_ADMIN -> "Administrator (Full Access)";
            default               -> "Administrator (Level " + adminLevel + ")";
        };
    }

    /**
     * Returns a string representation that includes the admin level.
     * Overrides {@link User#toString()} and reuses parent data via
     * {@code super} getters — no field duplication.
     */
    @Override
    public String toString() {
        return "AdminUser{" +
               "id="              + getId()    +
               ", name='"         + getName()  + '\'' +
               ", email='"        + getEmail() + '\'' +
               ", adminLevel="    + adminLevel +
               ", roleDesc='"     + getRoleDescription() + '\'' +
               '}';
    }

    // ----------------------------------------------------------------
    // AdminUser-specific getter / setters
    // ----------------------------------------------------------------

    /** @return the numeric privilege level of this administrator */
    public int getAdminLevel() { return adminLevel; }

    /**
     * @param adminLevel privilege level; use {@link #LEVEL_AUDITOR}
     *                   or {@link #LEVEL_FULL_ADMIN}
     */
    public void setAdminLevel(int adminLevel) { this.adminLevel = adminLevel; }

    // ----------------------------------------------------------------
    // Privilege-check helpers
    // (called by service / UI layers in a polymorphic manner)
    // ----------------------------------------------------------------

    /**
     * Returns {@code true} for every admin level – all admins can view
     * any user's expense records.
     *
     * @return always {@code true} for an {@code AdminUser}
     */
    public boolean canViewAllUsers() {
        return true;   // all admin levels have at least read access
    }

    /**
     * Returns {@code true} only for full administrators ({@link #LEVEL_FULL_ADMIN}).
     * Auditors ({@link #LEVEL_AUDITOR}) may not modify or delete data.
     *
     * @return {@code true} if this admin has write/delete privileges
     */
    public boolean canManageUsers() {
        return adminLevel >= LEVEL_FULL_ADMIN;
    }
}
