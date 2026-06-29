package edu.univ.erp.auth;

import edu.univ.erp.data.DatabaseConnector;
import edu.univ.erp.domain.User;

import java.sql.*;

public class AuthDAOImpl implements AuthDAO {

    private static final long LOCKOUT_DURATION_MS = 60 * 1000; // 1 Minute

    @Override
    public User login(String username, String password) throws SQLException, AuthException {
        String sql = "SELECT user_id, role, password_hash, status, failed_attempts, lockout_time FROM users_auth WHERE username = ?";

        try (Connection conn = DatabaseConnector.getAuthConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    int userId = rs.getInt("user_id");
                    String role = rs.getString("role");
                    String storedHash = rs.getString("password_hash");
                    int attempts = rs.getInt("failed_attempts");
                    Timestamp lockoutTime = rs.getTimestamp("lockout_time");

                    // 1. CHECK LOCKOUT STATUS
                    if (attempts >= 5) {
                        if (lockoutTime != null) {
                            long timeDiff = System.currentTimeMillis() - lockoutTime.getTime();
                            if (timeDiff < LOCKOUT_DURATION_MS) {
                                long secondsLeft = (LOCKOUT_DURATION_MS - timeDiff) / 1000;
                                // Throw exception WITH time info
                                throw new AuthException("Account locked.", secondsLeft);
                            } else {
                                // Timer expired! Auto-unlock
                                resetFailedAttempts(userId);
                                attempts = 0;
                            }
                        }
                    }

                    // 2. VERIFY PASSWORD
                    if (PasswordHasher.checkPassword(password, storedHash)) {
                        if (attempts > 0) resetFailedAttempts(userId);
                        return new User(userId, username, role);
                    } else {
                        // 3. HANDLE FAILURE
                        int newAttempts = attempts + 1;
                        handleFailedLogin(userId, newAttempts);

                        if (newAttempts >= 5) {
                            // Immediate feedback on the 5th fail
                            throw new AuthException("Account is now LOCKED.", 60);
                        } else {
                            throw new AuthException("Incorrect username or password.");
                        }
                    }
                } else {
                    throw new AuthException("Incorrect username or password.");
                }
            }
        }
    }

    private void handleFailedLogin(int userId, int newCount) throws SQLException {
        String sql;
        if (newCount >= 5) {
            // Lock it now! Set the timestamp.
            sql = "UPDATE users_auth SET failed_attempts = ?, status = 'Locked', lockout_time = NOW() WHERE user_id = ?";
        } else {
            // Just increment
            sql = "UPDATE users_auth SET failed_attempts = ? WHERE user_id = ?";
        }

        try (Connection conn = DatabaseConnector.getAuthConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, newCount);
            stmt.setInt(2, userId);
            stmt.executeUpdate();
        }
    }

    private void resetFailedAttempts(int userId) throws SQLException {
        // Clear attempts and the timestamp
        String sql = "UPDATE users_auth SET failed_attempts = 0, status = 'Active', lockout_time = NULL WHERE user_id = ?";
        try (Connection conn = DatabaseConnector.getAuthConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.executeUpdate();
        }
    }

    @Override
    public int createUser(String username, String role, String passwordHash) throws SQLException {
        String sql = "INSERT INTO users_auth (username, role, password_hash, status, failed_attempts) VALUES (?, ?, ?, 'Active', 0)";
        try (Connection conn = DatabaseConnector.getAuthConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, username);
            stmt.setString(2, role);
            stmt.setString(3, passwordHash);
            int rows = stmt.executeUpdate();
            if (rows == 0) return -1;
            try (ResultSet gk = stmt.getGeneratedKeys()) {
                if (gk.next()) return gk.getInt(1); else return -1;
            }
        }
    }

    @Override
    public int getUserIdByUsername(String username) throws SQLException {
        String sql = "SELECT user_id FROM users_auth WHERE username = ?";
        try (Connection conn = DatabaseConnector.getAuthConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return rs.getInt("user_id");
            }
        }
        return -1;
    }

    @Override
    public String getPasswordHash(int userId) throws SQLException {
        String sql = "SELECT password_hash FROM users_auth WHERE user_id = ?";
        try (Connection conn = DatabaseConnector.getAuthConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return rs.getString("password_hash");
            }
        }
        return null;
    }

    @Override
    public void updatePassword(int userId, String newHash) throws SQLException {
        // Resetting password also UNLOCKS the account
        String sql = "UPDATE users_auth SET password_hash = ?, status = 'Active', failed_attempts = 0 WHERE user_id = ?";
        try (Connection conn = DatabaseConnector.getAuthConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, newHash);
            stmt.setInt(2, userId);
            stmt.executeUpdate();
        }
    }

    @Override
    public void updateUsername(int userId, String newUsername) throws SQLException {
        String sql = "UPDATE users_auth SET username = ? WHERE user_id = ?";
        try (Connection conn = DatabaseConnector.getAuthConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, newUsername);
            stmt.setInt(2, userId);
            stmt.executeUpdate();
        }
    }
}