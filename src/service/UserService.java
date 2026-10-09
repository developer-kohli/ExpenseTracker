package service;

import model.User;

import java.util.Optional;

/**
 * Business-logic contract for user management in ExpenseTracker.
 *
 * <h3>OOP role – Interfaces (demonstrated here)</h3>
 * <p>The Swing UI depends only on this interface type, never on the
 * concrete {@link UserServiceImpl}.  This decoupling means:
 * <ul>
 *   <li>The UI can be written and compiled independently of the DB.</li>
 *   <li>The implementation can be replaced (e.g. a stub for UI testing)
 *       without changing a single line of UI code.</li>
 * </ul>
 *
 * <h3>Layer responsibility</h3>
 * <p>This interface (and its implementation) sit between the UI and the
 * DAO layer:
 * <pre>
 *   UI (Swing)  →  UserService  →  UserDAO  →  MySQL
 * </pre>
 * The service layer owns:
 * <ul>
 *   <li>Input validation (blank checks, e-mail format, password length)</li>
 *   <li>Password hashing before storage</li>
 *   <li>Translation of {@link java.sql.SQLException} →
 *       {@link ServiceException}</li>
 * </ul>
 * It does <b>not</b> own SQL, Swing, or presentation logic.
 */
// Interface: Business-logic contract for user authentication and management
// Abstraction: Shields UI from credential validation, password hashing, and exception handling
public interface UserService {

    /**
     * Validates the supplied credentials and registers a new user.
     *
     * <p>The service hashes the plain-text {@code password} before
     * passing it to the DAO — the DAO never receives a plain-text value.
     *
     * @param name     full display name (must not be blank)
     * @param email    unique e-mail address (must contain {@code @})
     * @param password plain-text password (minimum 6 characters)
     * @return {@code true} if registration succeeded
     * @throws ServiceException if validation fails, if the e-mail is
     *         already registered, or if a database error occurs
     */
    boolean registerUser(String name, String email, String password)
            throws ServiceException;

    /**
     * Authenticates a user with the supplied credentials.
     *
     * <p>The service hashes {@code password} and delegates credential
     * comparison to the DAO.
     *
     * @param email    the user's registered e-mail address
     * @param password the plain-text password to verify
     * @return an {@link Optional} containing the authenticated {@link User},
     *         or {@link Optional#empty()} if credentials are invalid
     * @throws ServiceException if validation fails or a database error occurs
     */
    Optional<User> login(String email, String password) throws ServiceException;

    /**
     * Retrieves a user by their primary key.
     *
     * @param id the database surrogate key
     * @return an {@link Optional} containing the {@link User} if found,
     *         or {@link Optional#empty()} if no user exists for {@code id}
     * @throws ServiceException if a database error occurs
     */
    Optional<User> getUserById(int id) throws ServiceException;
}
