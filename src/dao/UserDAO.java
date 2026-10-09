package dao;

import model.User;

import java.sql.SQLException;
import java.util.Optional;

/**
 * Contract for all user persistence operations in ExpenseTracker.
 *
 * <h3>OOP role – Interfaces (demonstrated here)</h3>
 * <p>The service layer ({@code service.AuthService}) depends only on
 * this interface type, never on the concrete {@link MySQLUserDAO}.
 * This mirrors the same decoupling shown in {@link ExpenseDAO} and
 * allows the implementation to be swapped (e.g. for testing) without
 * touching a single line of business logic.
 *
 * <h3>Exception strategy</h3>
 * <ul>
 *   <li>Methods throw {@link SQLException} so that database errors
 *       are surfaced to the <em>service</em> layer, which translates
 *       them into user-friendly messages before reaching the UI.</li>
 *   <li>Duplicate email registration produces a
 *       {@link DuplicateEmailException} (a checked sub-class of
 *       {@code SQLException}) so callers can distinguish that specific
 *       condition without inspecting raw SQL error codes.</li>
 * </ul>
 */
// Interface: Defines persistence contract for user authentication and management
// Abstraction: Hides user table queries, credential comparisons, and SQL statements
public interface UserDAO {

    /**
     * Persists a new user to the database.
     *
     * <p>On success the database-generated primary key is written back
     * into {@code user} via {@link model.BaseEntity#setId(int)}.
     *
     * @param user the user to register ({@code id} must be 0 / unset;
     *             {@code password} must already be hashed)
     * @return {@code true} if the row was inserted successfully
     * @throws DuplicateEmailException if {@code user.email} already exists
     *         in the {@code users} table (MySQL error 1062 / SQLState 23000)
     * @throws SQLException for any other database error
     */
    boolean registerUser(User user) throws SQLException;

    /**
     * Looks up a user by their e-mail address.
     *
     * @param email the unique e-mail address to search for
     * @return an {@link Optional} containing the {@link User} if found,
     *         or {@link Optional#empty()} when no matching row exists
     * @throws SQLException if a database error occurs
     */
    Optional<User> findUserByEmail(String email) throws SQLException;

    /**
     * Authenticates a user by verifying their e-mail and password.
     *
     * <p><b>Password contract</b>: the {@code password} parameter
     * received here is the <em>plain-text</em> value entered by the
     * user.  This method fetches the stored hash and delegates the
     * BCrypt comparison to the caller (service layer) so that the DAO
     * remains stateless and free of security-library dependencies.
     *
     * <p>Concretely: the implementation retrieves the row by e-mail and
     * performs a direct {@link String#equals} comparison against the
     * stored value.  When the service layer introduces BCrypt hashing,
     * only {@code AuthService.login()} needs to change — this interface
     * and {@link MySQLUserDAO} stay the same.
     *
     * @param email    the user's e-mail address
     * @param password the plain-text password to verify against the stored value
     * @return an {@link Optional} containing the authenticated {@link User},
     *         or {@link Optional#empty()} if credentials do not match
     * @throws SQLException if a database error occurs
     */
    Optional<User> authenticateUser(String email, String password) throws SQLException;

    /**
     * Fetches a user by their primary key.
     *
     * @param id the database surrogate key
     * @return an {@link Optional} containing the {@link User} if found,
     *         or {@link Optional#empty()} when no row exists for {@code id}
     * @throws SQLException if a database error occurs
     */
    Optional<User> getUserById(int id) throws SQLException;

    // ----------------------------------------------------------------
    // Nested exception type
    // ----------------------------------------------------------------

    /**
     * Thrown when an attempt is made to register an e-mail address
     * that is already present in the {@code users} table.
     *
     * <p>Extends {@link SQLException} so it travels through the same
     * {@code throws} declarations and can be caught specifically by the
     * service layer to produce the message "Email already registered."
     */
    class DuplicateEmailException extends SQLException {

        private static final long serialVersionUID = 1L;

        private final String email;

        /**
         * @param email the duplicate e-mail address
         */
        public DuplicateEmailException(String email) {
            super("Email address is already registered: " + email,
                  "23000",   // SQLState – Integrity Constraint Violation
                  1062);     // MySQL error code for duplicate entry
            this.email = email;
        }

        /** @return the e-mail address that caused the conflict */
        public String getEmail() { return email; }
    }
}
