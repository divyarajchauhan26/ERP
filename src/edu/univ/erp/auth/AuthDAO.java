package edu.univ.erp.auth;

import edu.univ.erp.domain.User;
import java.sql.SQLException;

public interface AuthDAO {

    User login(String username, String password) throws SQLException, AuthException;

    int createUser(String username, String role, String passwordHash) throws SQLException;

    int getUserIdByUsername(String username) throws SQLException;

    String getPasswordHash(int userId) throws SQLException;

    void updatePassword(int userId, String newHash) throws SQLException;

    void updateUsername(int userId, String newUsername) throws SQLException;

    // REMOVED: unlockUser
}