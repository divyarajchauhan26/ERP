package edu.univ.erp.data;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnector {

    // --- Auth DB Connection ---
    // Make sure to change "YOUR_ROOT_PASSWORD" to the password you set
    private static final String AUTH_DB_URL = "jdbc:mysql://localhost:3306/university_auth_db";
    private static final String AUTH_DB_USER = "root";
    private static final String AUTH_DB_PASSWORD = "Admin@12"; // <-- CHANGE THIS

    // --- ERP DB Connection ---
    // Make sure to change "YOUR_ROOT_PASSWORD"
    private static final String ERP_DB_URL = "jdbc:mysql://localhost:3306/university_erp_db";
    private static final String ERP_DB_USER = "root";
    private static final String ERP_DB_PASSWORD = "Admin@12"; // <-- CHANGE THIS

    // --- NEW GETTERS FOR SHELL COMMANDS ---
    public static String getErpDbName() { return "university_erp_db"; }
    public static String getErpDbUser() { return ERP_DB_USER; }
    public static String getErpDbPassword() { return ERP_DB_PASSWORD; }
    // --- END NEW GETTERS ---

    /**
     * Gets a connection to the Auth DB (for logins).
     */
    public static Connection getAuthConnection() throws SQLException {
        try {
            // This line "loads" the driver you added
            Class.forName("com.mysql.cj.jdbc.Driver");
            return DriverManager.getConnection(AUTH_DB_URL, AUTH_DB_USER, AUTH_DB_PASSWORD);
        } catch (ClassNotFoundException e) {
            System.err.println("MySQL JDBC Driver not found!");
            throw new SQLException("Driver not found", e);
        }
    }

    /**
     * Gets a connection to the ERP DB (for all other data).
     */
    public static Connection getErpConnection() throws SQLException {
        try {
            // This line "loads" the driver you added
            Class.forName("com.mysql.cj.jdbc.Driver");
            return DriverManager.getConnection(ERP_DB_URL, ERP_DB_USER, ERP_DB_PASSWORD);
        } catch (ClassNotFoundException e) {
            System.err.println("MySQL JDBC Driver not found!");
            throw new SQLException("Driver not found", e);
        }
    }

    // A simple test method you can run
    public static void main(String[] args) {
        try (Connection authConn = getAuthConnection();
             Connection erpConn = getErpConnection()) {

            if (authConn != null) {
                System.out.println("Successfully connected to Auth DB!");
            }
            if (erpConn != null) {
                System.out.println("Successfully connected to ERP DB!");
            }

        } catch (SQLException e) {
            System.err.println("Connection failed!");
            e.printStackTrace();
        }
    }
}