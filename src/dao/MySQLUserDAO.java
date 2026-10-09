package dao;

import model.User;

import java.sql.*;
import java.util.Optional;

/**
 * MySQL / JDBC implementation of {@link UserDAO}.
 *
 * <h3>OOP roles demonstrated here</h3>
 * <ul>
 *   <li><b>Interface implementation</b> – {@code implements UserDAO}
 *       keeps the service layer decoupled from MySQL specifics.</li>
 *   <li><b>Polymorphism</b> – a field of type {@link UserDAO} holds
 *       a {@code MySQLUserDAO} at runtime; callers never need to
 *       import this concrete class.</li>
 * </ul>
 *
 * <h3>JDBC discipline</h3>
 * <ul>
 *   <li>Every parameterised query uses {@link PreparedStatement} –
 *       no string concatenation of user-supplied values.</li>
 *   <li>Every {@link Connection}, {@link PreparedStatement} and
 *       {@link ResultSet} is opened in a <em>try-with-resources</em>
 *       block and closed automatically.</li>
 * </ul>
 *
 * <h3>Exception handling</h3>
 * <ul>
 *   <li>MySQL error 1062 / SQLState {@code 23000} (duplicate UNIQUE
 *       key) is caught and re-thrown as
 *       {@link UserDAO.DuplicateEmailException} so the service layer
 *       can display "Email already registered" without parsing raw SQL
 *       error codes.</li>
 *   <li>All other {@link SQLException}s propagate to the service layer,
 *       which translates them into user-friendly messages.  Raw SQL
 *       exceptions never reach the Swing UI directly.</li>
 * </ul>
 *
 * <h3>Password handling</h3>
 * <p>This class stores and compares password values exactly as supplied.
 * Hashing (BCrypt) is the responsibility of {@code service.AuthService}.
 * The DAO receives an already-hashed string on registration and
 * performs a direct {@link String#equals} comparison on authentication.
 * When BCrypt is introduced in the service layer, this class requires
 * <em>no changes</em>.
 */
// Polymorphism: Concrete JDBC implementation of UserDAO
public class MySQLUserDAO implements UserDAO {

    // ----------------------------------------------------------------
    // SQL constants – all use ? placeholders, never concatenation
    // ----------------------------------------------------------------

    private static final String SQL_INSERT =
            "INSERT INTO users (name, email, password) VALUES (?, ?, ?)";

    private static final String SQL_SELECT_BY_EMAIL =
            "SELECT id, name, email, password " +
            "FROM users " +
            "WHERE email = ?";

    private static final String SQL_SELECT_BY_ID =
            "SELECT id, name, email, password " +
            "FROM users " +
            "WHERE id = ?";

    // ================================================================
    // Interface implementation
    // ================================================================

    /**
     * {@inheritDoc}
     *
     * <p>Uses {@link Statement#RETURN_GENERATED_KEYS} to read the
     * auto-incremented {@code id} and write it back into {@code user}.
     *
     * <p>Duplicate e-mail addresses produce a
     * {@link UserDAO.DuplicateEmailException} instead of a raw
     * {@link SQLIntegrityConstraintViolationException}, shielding
     * callers from MySQL-specific error codes.
     */
    @Override
    public boolean registerUser(User user) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     SQL_INSERT, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, user.getName());
            ps.setString(2, user.getEmail());
            ps.setString(3, user.getPassword()); // must already be hashed by caller

            int rowsAffected;
            try {
                rowsAffected = ps.executeUpdate();
            } catch (SQLIntegrityConstraintViolationException e) {
                // MySQL error 1062 – UNIQUE constraint on users.email violated.
                // Re-throw as a domain-specific, UI-friendly exception.
                throw new DuplicateEmailException(user.getEmail());
            }

            if (rowsAffected == 1) {
                // Write the DB-generated primary key back into the model object.
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) {
                        user.setId(keys.getInt(1));
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
     * <p>Returns {@link Optional#empty()} — never {@code null} — when no
     * row matches, eliminating the risk of {@code NullPointerException}
     * in the caller.
     */
    @Override
    public Optional<User> findUserByEmail(String email) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_SELECT_BY_EMAIL)) {

            ps.setString(1, email);

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
     * <p><b>Implementation note</b>: the method fetches the user row by
     * e-mail first, then compares the stored password string with the
     * supplied {@code password} using {@link String#equals}.
     *
     * <p>When {@code service.AuthService} introduces BCrypt hashing,
     * only that service method changes — the comparison here will be
     * replaced with {@code BCrypt.checkpw(password, storedHash)}.  This
     * DAO file will not need to be modified at that point.
     *
     * <p>Returns {@link Optional#empty()} for both "user not found"
     * and "wrong password" to avoid leaking which part of the
     * credential was incorrect (a standard security practice).
     */
    @Override
    public Optional<User> authenticateUser(String email, String password)
            throws SQLException {

        // Step 1: fetch the user record by email.
        Optional<User> userOpt = findUserByEmail(email);
        if (userOpt.isEmpty()) {
            // No account with this email – return empty without further work.
            return Optional.empty();
        }

        User user = userOpt.get();

        // Step 2: compare the supplied password against the stored value.
        // NOTE: Replace this with BCrypt.checkpw(password, user.getPassword())
        //       when the AuthService introduces hashing in a later phase.
        //       The DAO interface and this class will not need to change.
        if (password != null && password.equals(user.getPassword())) {
            return Optional.of(user);
        }

        // Credentials did not match – return empty (same as "user not found"
        // to avoid leaking which part of the credentials was wrong).
        return Optional.empty();
    }

    /**
     * {@inheritDoc}
     *
     * <p>Returns {@link Optional#empty()} when no row exists for the
     * given {@code id}.
     */
    @Override
    public Optional<User> getUserById(int id) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_SELECT_BY_ID)) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        }
        return Optional.empty();
    }

    // ================================================================
    // Private helpers
    // ================================================================

    /**
     * Maps one {@link ResultSet} row to a {@link User} object.
     *
     * <p>This is the single location in this class where column names
     * appear, making schema renames straightforward to track.
     *
     * @param rs a {@link ResultSet} positioned at a valid row
     * @return a fully populated {@link User}
     * @throws SQLException if any column cannot be read
     */
    private User mapRow(ResultSet rs) throws SQLException {
        int    id       = rs.getInt("id");
        String name     = rs.getString("name");
        String email    = rs.getString("email");
        String password = rs.getString("password"); // hashed value

        // Returns a base User instance.
        // The service layer can promote this to RegularUser or AdminUser
        // by inspecting a role field once that column is added to the schema.
        return new User(id, name, email, password);
    }
}
