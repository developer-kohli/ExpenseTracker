package service;

import dao.MySQLUserDAO;
import dao.UserDAO;
import model.User;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.HexFormat;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * Concrete implementation of {@link UserService}.
 *
 * <h3>OOP roles demonstrated here</h3>
 * <ul>
 *   <li><b>Interface implementation</b> – {@code implements UserService};
 *       the UI holds a {@link UserService} reference, never this type.</li>
 *   <li><b>Polymorphism</b> – a {@code UserService} variable holds a
 *       {@code UserServiceImpl} at runtime; any replacement
 *       implementation would work without changing the UI.</li>
 *   <li><b>Dependency injection via constructor</b> – the {@link UserDAO}
 *       is injected, so the concrete DAO class can be changed (e.g. a
 *       test stub) without touching this class.</li>
 * </ul>
 *
 * <h3>Responsibilities</h3>
 * <ul>
 *   <li>Input validation (name, e-mail format, password length)</li>
 *   <li>Password hashing before any DAO call
 *       (SHA-256 now; upgrade path to BCrypt is documented inline)</li>
 *   <li>Translating {@link SQLException} → {@link ServiceException}
 *       so raw SQL errors never reach the Swing UI</li>
 * </ul>
 *
 * <h3>No Swing code in this class</h3>
 * <p>Zero imports from {@code javax.swing} or {@code java.awt}.
 */
// Polymorphism: Concrete implementation of the UserService interface
public class UserServiceImpl implements UserService {

    // ----------------------------------------------------------------
    // Validation constants
    // ----------------------------------------------------------------

    /** Minimum accepted password length. */
    private static final int MIN_PASSWORD_LENGTH = 6;

    /**
     * Loose e-mail pattern: requires at least one character before and
     * after the {@code @}, and a dot somewhere in the domain part.
     */
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    // ----------------------------------------------------------------
    // DAO dependency (injected – never instantiated here)
    // ----------------------------------------------------------------

    // Encapsulation: DAO reference held as private final field
    private final UserDAO userDAO;

    // ----------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------

    /**
     * Default constructor – creates a {@link MySQLUserDAO} internally.
     * Convenient for production use.
     */
    public UserServiceImpl() {
        this(new MySQLUserDAO());
    }

    /**
     * Dependency-injection constructor.
     * Use this constructor in unit tests to inject a stub/mock DAO.
     *
     * @param userDAO the DAO implementation to delegate persistence to
     */
    public UserServiceImpl(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    // ================================================================
    // UserService implementation
    // ================================================================

    /**
     * {@inheritDoc}
     *
     * <p><b>Business rules enforced</b>:
     * <ol>
     *   <li>Name must not be blank.</li>
     *   <li>E-mail must match a basic {@code user@domain.tld} pattern.</li>
     *   <li>Password must be at least {@value #MIN_PASSWORD_LENGTH} characters.</li>
     * </ol>
     * <p>The password is hashed with SHA-256 before being passed to the
     * DAO.  To upgrade to BCrypt: replace {@link #hashPassword(String)}
     * with {@code BCrypt.hashpw(password, BCrypt.gensalt())} — this
     * method body stays the same.
     */
    @Override
    public boolean registerUser(String name, String email, String password)
            throws ServiceException {

        // ── 1. Validate inputs ───────────────────────────────────────
        validateName(name);
        validateEmail(email);
        validatePassword(password);

        // ── 2. Hash password (plain text never stored) ───────────────
        String hashedPassword = hashPassword(password);

        // ── 3. Build the domain object ───────────────────────────────
        User user = new User(name.trim(), email.trim().toLowerCase(), hashedPassword);

        // ── 4. Delegate to DAO; translate exceptions for the UI ──────
        try {
            return userDAO.registerUser(user);

        } catch (UserDAO.DuplicateEmailException e) {
            // Business rule: e-mail already taken → user-friendly message
            throw new ServiceException(
                    "An account with email '" + email + "' already exists. "
                    + "Please use a different email or log in.", e);

        } catch (SQLException e) {
            throw new ServiceException(
                    "Registration failed due to a database error. Please try again.", e);
        }
    }

    /**
     * {@inheritDoc}
     *
     * <p>Hashes the supplied plain-text password with the same algorithm
     * used at registration before passing it to the DAO for comparison.
     */
    @Override
    public Optional<User> login(String email, String password)
            throws ServiceException {

        // ── 1. Basic input checks ────────────────────────────────────
        if (email == null || email.isBlank()) {
            throw new ServiceException("Email address must not be empty.");
        }
        if (password == null || password.isBlank()) {
            throw new ServiceException("Password must not be empty.");
        }

        // ── 2. Hash the supplied password to match stored hash ───────
        String hashedPassword = hashPassword(password);

        // ── 3. Delegate credential check to DAO ─────────────────────
        try {
            return userDAO.authenticateUser(email.trim().toLowerCase(), hashedPassword);

        } catch (SQLException e) {
            throw new ServiceException(
                    "Login failed due to a database error. Please try again.", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<User> getUserById(int id) throws ServiceException {
        if (id <= 0) {
            throw new ServiceException("Invalid user ID: " + id);
        }
        try {
            return userDAO.getUserById(id);
        } catch (SQLException e) {
            throw new ServiceException(
                    "Could not retrieve user. Please try again.", e);
        }
    }

    // ================================================================
    // Private validation helpers
    // ================================================================

    private void validateName(String name) throws ServiceException {
        if (name == null || name.isBlank()) {
            throw new ServiceException("Name must not be empty.");
        }
        if (name.trim().length() > 100) {
            throw new ServiceException("Name must not exceed 100 characters.");
        }
    }

    private void validateEmail(String email) throws ServiceException {
        if (email == null || email.isBlank()) {
            throw new ServiceException("Email address must not be empty.");
        }
        if (!EMAIL_PATTERN.matcher(email.trim()).matches()) {
            throw new ServiceException(
                    "'" + email + "' is not a valid email address.");
        }
        if (email.trim().length() > 100) {
            throw new ServiceException("Email must not exceed 100 characters.");
        }
    }

    private void validatePassword(String password) throws ServiceException {
        if (password == null || password.isBlank()) {
            throw new ServiceException("Password must not be empty.");
        }
        if (password.length() < MIN_PASSWORD_LENGTH) {
            throw new ServiceException(
                    "Password must be at least " + MIN_PASSWORD_LENGTH + " characters long.");
        }
    }

    // ================================================================
    // Password hashing
    // ================================================================

    /**
     * Hashes a plain-text password using SHA-256.
     *
     * <p><b>Upgrade path to BCrypt</b>: replace the body of this method
     * with:
     * <pre>{@code
     * // Registration:   return BCrypt.hashpw(plainText, BCrypt.gensalt(12));
     * // Login:          use   BCrypt.checkpw(plainText, storedHash)
     *                    in UserServiceImpl.login() instead of
     *                    passing the hashed value to the DAO.
     * }</pre>
     * All callers of this helper remain unchanged.
     *
     * @param plainText the raw password entered by the user
     * @return a 64-character lowercase hex SHA-256 digest
     * @throws ServiceException if the JVM unexpectedly lacks SHA-256 support
     */
    private String hashPassword(String plainText) throws ServiceException {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(
                    plainText.getBytes(StandardCharsets.UTF_8));
            // HexFormat is available from Java 17+
            return HexFormat.of().formatHex(hashBytes);
        } catch (NoSuchAlgorithmException e) {
            // SHA-256 is mandated by the Java specification — this should never happen.
            throw new ServiceException("Critical: password hashing unavailable.", e);
        }
    }
}
