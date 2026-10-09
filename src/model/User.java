package model;

/**
 * Domain object representing a registered application user.
 *
 * <h3>Inheritance hierarchy</h3>
 * <pre>
 *   BaseEntity  (abstract – surrogate PK)
 *   └── User    (this class – core user data + role API)
 *       ├── RegularUser  – standard expense-tracking user
 *       └── AdminUser    – administrator with elevated privileges
 * </pre>
 *
 * <h3>OOP notes</h3>
 * <ul>
 *   <li><b>Inheritance</b>: extends {@link BaseEntity} to inherit
 *       the surrogate {@code id} and its accessors.  Also acts as the
 *       <em>parent</em> for {@link RegularUser} and {@link AdminUser}.</li>
 *   <li><b>Polymorphism</b>: {@link #getRole()} and
 *       {@link #getRoleDescription()} are declared here with default
 *       implementations and are overridden in each sub-class, so a
 *       {@code User} reference can exhibit different behaviour at
 *       runtime depending on the actual concrete type.</li>
 *   <li><b>Encapsulation</b>: all fields are {@code private}; access
 *       is through getters/setters only.</li>
 *   <li><b>No JDBC</b>: this class is a pure POJO – persistence is
 *       handled exclusively by {@code dao.UserDAO}.</li>
 * </ul>
 *
 * <h3>Password contract</h3>
 * <p>The {@code password} field always stores a <em>hashed</em> value
 * (BCrypt or similar).  Plain-text passwords are hashed in the
 * {@code service} layer before being set here and must never be
 * stored or logged in plain text.
 */
// Inheritance: User extends abstract BaseEntity
public class User extends BaseEntity {

    // ----------------------------------------------------------------
    // Fields (Encapsulation: private fields accessed via getters/setters)
    // ----------------------------------------------------------------

    // Encapsulation
    private String name;
    private String email;
    /** BCrypt-hashed password. Never store plain text here. */
    private String password;

    // ----------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------

    /**
     * No-arg constructor.
     * Required for reflection-based frameworks and list-population loops.
     */
    public User() {
        super();
    }

    /**
     * Validation-friendly constructor – used when <em>creating</em> a new
     * user before the database assigns an ID.
     *
     * <p>The {@code id} is left as {@code 0}; the DAO sets it after the
     * {@code INSERT} succeeds.
     *
     * @param name     full display name (non-blank)
     * @param email    unique e-mail address (non-blank)
     * @param password already-hashed password (non-blank)
     */
    public User(String name, String email, String password) {
        super();
        this.name     = name;
        this.email    = email;
        this.password = password;
    }

    /**
     * Full constructor – used when <em>loading</em> an existing user
     * from the database (id is already known).
     *
     * @param id       database primary key
     * @param name     full display name
     * @param email    unique e-mail address
     * @param password hashed password
     */
    public User(int id, String name, String email, String password) {
        super(id);
        this.name     = name;
        this.email    = email;
        this.password = password;
    }

    // ----------------------------------------------------------------
    // Getters & Setters
    // ----------------------------------------------------------------

    /** @return the user's full display name */
    public String getName() { return name; }

    /** @param name full display name; must not be blank */
    public void setName(String name) { this.name = name; }

    /** @return the user's unique e-mail address */
    public String getEmail() { return email; }

    /** @param email unique e-mail address; must not be blank */
    public void setEmail(String email) { this.email = email; }

    /**
     * @return the BCrypt-hashed password stored for this user
     * @apiNote Never log or display this value in the UI
     */
    public String getPassword() { return password; }

    /**
     * @param password BCrypt-hashed password; must not be blank.
     *                 Callers in the service layer must hash the raw
     *                 password before calling this setter.
     */
    public void setPassword(String password) { this.password = password; }

    // ----------------------------------------------------------------
    // Role API  ← overridden by RegularUser and AdminUser
    // ----------------------------------------------------------------

    /**
     * Returns a short role identifier for this user.
     *
     * <p><b>Inheritance / Polymorphism</b>: this base implementation
     * returns {@code "USER"}.  Sub-classes override it to return their
     * specific role string, so code that holds a {@code User} reference
     * automatically gets the correct role at runtime:
     * <pre>{@code
     * User u = new AdminUser(...);
     * System.out.println(u.getRole()); // prints "ADMIN"
     * }</pre>
     *
     * @return role identifier string
     */
    // Polymorphism: Base role method overridden in subclasses (RegularUser, AdminUser)
    public String getRole() {
        // Base / fallback role — sub-classes MUST override this.
        return "USER";
    }

    /**
     * Returns a human-readable description of the user's role.
     * Overridden in {@link RegularUser} and {@link AdminUser} to
     * provide role-specific descriptions.
     *
     * @return descriptive role label
     */
    public String getRoleDescription() {
        return "Registered User";
    }

    // ----------------------------------------------------------------
    // Object overrides
    // ----------------------------------------------------------------

    /**
     * Returns a safe string representation that omits the password hash.
     *
     * @return e.g. {@code User[REGULAR_USER]{id=1, name='Alice', email='alice@example.com'}}
     */
    @Override
    public String toString() {
        return "User[" + getRole() + "]{"
               + "id=" + getId()
               + ", name='" + name + '\''
               + ", email='" + email + '\''
               + '}';
        // password intentionally excluded for security
    }
}
