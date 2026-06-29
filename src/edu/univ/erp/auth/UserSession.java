package edu.univ.erp.auth;

import edu.univ.erp.domain.User;

// This class will hold the currently logged-in user's data.
// We use the "Singleton" pattern to ensure there's only ONE session.
public class UserSession {

    private static UserSession instance; // The single instance
    private User currentUser;

    // Private constructor so no one else can create one
    private UserSession() { }

    /**
     * Gets the single instance of the session.
     */
    public static UserSession getInstance() {
        if (instance == null) {
            instance = new UserSession();
        }
        return instance;
    }

    /**
     * Creates a new session for the logged-in user.
     * @param user The User object from the domain package.
     */
    public void createUserSession(User user) {
        this.currentUser = user;
    }

    /**
     * Destroys the current session (for logout).
     */
    public void clearSession() {
        this.currentUser = null;
    }

    /**
     * Gets the currently logged-in user.
     * @return The User object, or null if no one is logged in.
     */
    public User getCurrentUser() {
        return this.currentUser;
    }

    /**
     * Checks if a user is currently logged in.
     */
    public boolean isLoggedIn() {
        return this.currentUser != null;
    }
}