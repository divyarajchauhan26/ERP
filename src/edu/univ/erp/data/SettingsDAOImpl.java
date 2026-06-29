package edu.univ.erp.data;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;

public class SettingsDAOImpl implements SettingsDAO {

    private static final String MAINTENANCE_KEY = "maintenance_on";
    private static final String DROP_DEADLINE_KEY = "drop_deadline";
    private static final String REG_DEADLINE_KEY = "registration_deadline"; // <--- NEW KEY

    @Override
    public boolean isMaintenanceModeOn() throws SQLException {
        return getBooleanSetting(MAINTENANCE_KEY);
    }

    @Override
    public void setMaintenanceMode(boolean isOon) throws SQLException {
        updateSetting(MAINTENANCE_KEY, isOon ? "true" : "false");
    }

    @Override
    public LocalDate getDropDeadline() throws SQLException {
        return getDateSetting(DROP_DEADLINE_KEY);
    }

    @Override
    public void setDropDeadline(String dateString) throws SQLException {
        updateSetting(DROP_DEADLINE_KEY, dateString);
    }

    // --- NEW METHODS ---
    @Override
    public LocalDate getRegistrationDeadline() throws SQLException {
        return getDateSetting(REG_DEADLINE_KEY);
    }

    @Override
    public void setRegistrationDeadline(String dateString) throws SQLException {
        updateSetting(REG_DEADLINE_KEY, dateString);
    }

    @Override
    public LocalDate getDatabaseCurrentDate() throws SQLException {
        String sql = "SELECT CURRENT_DATE";
        try (Connection conn = DatabaseConnector.getErpConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) return rs.getDate(1).toLocalDate();
        }
        return LocalDate.now();
    }

    // --- Helpers to avoid repetitive code ---
    private boolean getBooleanSetting(String key) throws SQLException {
        String sql = "SELECT setting_value FROM settings WHERE setting_key = ?";
        try (Connection conn = DatabaseConnector.getErpConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, key);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return "true".equalsIgnoreCase(rs.getString("setting_value"));
            }
        }
        return false;
    }

    private LocalDate getDateSetting(String key) throws SQLException {
        String sql = "SELECT setting_value FROM settings WHERE setting_key = ?";
        try (Connection conn = DatabaseConnector.getErpConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, key);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return LocalDate.parse(rs.getString("setting_value"));
            }
        }
        return LocalDate.of(2099, 12, 31);
    }

    private void updateSetting(String key, String value) throws SQLException {
        String sql = "INSERT INTO settings (setting_key, setting_value) VALUES (?, ?) " +
                "ON DUPLICATE KEY UPDATE setting_value = VALUES(setting_value)";
        try (Connection conn = DatabaseConnector.getErpConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, key);
            stmt.setString(2, value);
            stmt.executeUpdate();
        }
    }
}