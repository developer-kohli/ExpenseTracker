package util;

import model.User;

/**
 * Singleton session manager for the ExpenseTracker application.
 *
 * <h3>Purpose & Architecture</h3>
 * <p>Maintains runtime state and identity for the currently authenticated user
 * across all Swing UI screens, eliminating the need to pass user objects or IDs
 * through every constructor or method signature.
 *
 * <h3>Security Contract</h3>
 * <p>To prevent accidental password exposure in memory or logs, the session
 * sanitizes the stored {@link User} object upon login (or explicitly clears
 * the password field), ensuring credentials are not retained in active session memory.
 *
 * <h3>Design Pattern</h3>
 * <p>Implemented as a thread-safe Singleton using lazy initialization and synchronization
 * where needed.
 */
public class SessionManager {

    // Encapsulation: Private static instance and private user field
    private static volatile SessionManager instance;

    // Encapsulation
    private User currentUser;

    /**
     * Private constructor to enforce singleton pattern.
     */
    private SessionManager() {
        this.currentUser = null;
    }

    /**
     * Returns the global singleton instance of {@link SessionManager}.
     *
     * @return the singleton instance
     */
    public static SessionManager getInstance() {
        if (instance == null) {
            synchronized (SessionManager.class) {
                if (instance == null) {
                    instance = new SessionManager();
                }
            }
        }
        return instance;
    }

    /**
     * Sets the currently logged-in user and clears any sensitive password information
     * before persisting in the session state.
     *
     * @param user the authenticated {@link User} object (or {@code null} to reset)
     */
    public synchronized void setCurrentUser(User user) {
        if (user != null) {
            // Strip password from memory for security hygiene
            user.setPassword(null);
            this.currentUser = user;
        } else {
            this.currentUser = null;
        }
    }

    /**
     * Returns the currently authenticated {@link User}.
     *
     * @return the current {@link User}, or {@code null} if no active session
     */
    public synchronized User getCurrentUser() {
        return currentUser;
    }

    /**
     * Returns the database primary key ID of the currently authenticated user.
     *
     * @return the current user ID, or {@code -1} if no user is logged in
     */
    public synchronized int getCurrentUserId() {
        return (currentUser != null) ? currentUser.getId() : -1;
    }

    /**
     * Checks if a user is currently authenticated in this session.
     *
     * @return {@code true} if a user is logged in; {@code false} otherwise
     */
    public synchronized boolean isLoggedIn() {
        return currentUser != null;
    }

    /**
     * Clears the active session and logs out the current user.
     */
    public synchronized void logout() {
        this.currentUser = null;
    }
}
