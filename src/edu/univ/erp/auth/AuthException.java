package edu.univ.erp.auth;

public class AuthException extends Exception {

    private long waitSeconds = 0; // Default is 0 (no wait)

    public AuthException(String message) {
        super(message);
    }

    // New constructor for lockout
    public AuthException(String message, long waitSeconds) {
        super(message);
        this.waitSeconds = waitSeconds;
    }

    public long getWaitSeconds() {
        return waitSeconds;
    }
}