package edu.univ.erp.data;

import java.sql.SQLException;
import java.time.LocalDate;

public interface SettingsDAO {

    boolean isMaintenanceModeOn() throws SQLException;
    void setMaintenanceMode(boolean isOon) throws SQLException;

    LocalDate getDropDeadline() throws SQLException;
    void setDropDeadline(String dateString) throws SQLException;

    // --- NEW METHODS FOR REGISTRATION DEADLINE ---
    LocalDate getRegistrationDeadline() throws SQLException;
    void setRegistrationDeadline(String dateString) throws SQLException;

    LocalDate getDatabaseCurrentDate() throws SQLException;
}