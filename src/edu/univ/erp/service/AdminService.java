package edu.univ.erp.service;

import edu.univ.erp.auth.AuthDAO;
import edu.univ.erp.auth.AuthDAOImpl;
import edu.univ.erp.auth.PasswordHasher;
import edu.univ.erp.data.*;
import edu.univ.erp.domain.Course;
import edu.univ.erp.domain.Instructor;
import edu.univ.erp.domain.Student;
import edu.univ.erp.service.ServiceException;
import edu.univ.erp.access.AccessControlService;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.List;

public class AdminService {

    private AuthDAO authDAO;
    private StudentDAO studentDAO;
    private InstructorDAO instructorDAO;
    private CourseDAO courseDAO;
    private EnrollmentDAO enrollmentDAO;
    private AccessControlService accessControl;

    // --- CRITICAL: CHANGE THIS PATH TO YOUR MYSQL BIN FOLDER ---
    private static final String MYSQL_BIN_PATH = "C:\\Program Files\\MySQL\\MySQL Server 8.0\\bin";
    // -----------------------------------------------------------

    public AdminService() {
        this.authDAO = new AuthDAOImpl();
        this.studentDAO = new StudentDAOImpl();
        this.instructorDAO = new InstructorDAOImpl();
        this.courseDAO = new CourseDAOImpl();
        this.enrollmentDAO = new EnrollmentDAOImpl();
        this.accessControl = new AccessControlService();
    }

    private void checkAdminPermission() throws ServiceException {
        if (!accessControl.isCurrentUserAdmin()) {
            throw new ServiceException("Permission Denied: Only Administrators can perform this action.");
        }
    }

    // --- Backup/Restore Methods ---

    public void backupErpDatabase(String filePath) throws ServiceException {
        checkAdminPermission();

        String dbName = DatabaseConnector.getErpDbName();
        String dbUser = DatabaseConnector.getErpDbUser();
        String dbPass = DatabaseConnector.getErpDbPassword();
        String dumpPath = new File(MYSQL_BIN_PATH, "mysqldump").getAbsolutePath();

        String[] command = {
                dumpPath,
                "--user=" + dbUser,
                "--password=" + dbPass,
                "--databases",
                dbName
        };

        try {
            ProcessBuilder pb = new ProcessBuilder(command);
            Process process = pb.start();

            // Read the output (the dump content)
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                 PrintWriter writer = new PrintWriter(filePath)) {
                String line;
                while ((line = reader.readLine()) != null) {
                    writer.println(line);
                }
            }

            int exitCode = process.waitFor();
            if (exitCode != 0) {
                // Read error output
                BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
                StringBuilder error = new StringBuilder();
                String line;
                while ((line = errorReader.readLine()) != null) {
                    error.append(line).append("\n");
                }
                throw new ServiceException("Backup failed with exit code " + exitCode + ": " + error.toString());
            }

        } catch (Exception e) {
            throw new ServiceException("Backup execution error: " + e.getMessage());
        }
    }

    public void restoreErpDatabase(String filePath) throws ServiceException {
        checkAdminPermission();

        String dbName = DatabaseConnector.getErpDbName();
        String dbUser = DatabaseConnector.getErpDbUser();
        String dbPass = DatabaseConnector.getErpDbPassword();
        String mysqlPath = new File(MYSQL_BIN_PATH, "mysql").getAbsolutePath();

        String[] command = {
                mysqlPath,
                "--user=" + dbUser,
                "--password=" + dbPass,
                dbName
        };

        try {
            ProcessBuilder pb = new ProcessBuilder(command);
            pb.redirectInput(new File(filePath)); // Redirect file content to MySQL input
            Process process = pb.start();

            int exitCode = process.waitFor();
            if (exitCode != 0) {
                // Read error output
                BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
                StringBuilder error = new StringBuilder();
                String line;
                while ((line = errorReader.readLine()) != null) {
                    error.append(line).append("\n");
                }
                throw new ServiceException("Restore failed with exit code " + exitCode + ": " + error.toString());
            }

        } catch (Exception e) {
            throw new ServiceException("Restore execution error: " + e.getMessage());
        }
    }

    // --- NEW METHOD: Delete Section with Enrollment Check ---
    public void deleteSection(int sectionId) throws SQLException, ServiceException {
        checkAdminPermission();

        // 1. Check for existing enrollments
        int enrollmentCount = enrollmentDAO.getEnrollmentCount(sectionId);
        if (enrollmentCount > 0) {
            // Blocked if students are enrolled.
            throw new ServiceException("Cannot delete section: " + enrollmentCount + " students are currently enrolled.");
        }

        // 2. Delete the section
        courseDAO.deleteSection(sectionId);
    }

    // --- NEW METHOD: Delete Course with Dependency Check ---
    public void deleteCourse(String courseCode) throws SQLException, ServiceException {
        checkAdminPermission();

        try {
            courseDAO.deleteCourse(courseCode);
        } catch (SQLException e) {
            // Check for MySQL/database Foreign Key constraint violation (e.g., error code 1451)
            if (e.getErrorCode() == 1451 || e.getErrorCode() == 1217) {
                throw new ServiceException("Cannot delete course '" + courseCode + "'. Sections are currently dependent on it.");
            }
            throw new ServiceException("Database error: Could not delete course.");
        }
    }

    // --- Existing Service Methods ---

    public List<Course> getAllCourses() throws SQLException {
        return courseDAO.getAllCourses();
    }

    public void createNewUser(String username, String password, String role, String rollOrDept, String program) throws SQLException, ServiceException {
        checkAdminPermission();

        String passwordHash = PasswordHasher.hashPassword(password);
        int newUserId = authDAO.createUser(username, role, passwordHash);

        if (newUserId == -1) {
            throw new ServiceException("Could not create user in Auth DB.");
        }

        if ("Student".equals(role)) {
            studentDAO.createStudentProfile(newUserId, rollOrDept, program);
        } else if ("Instructor".equals(role)) {
            instructorDAO.createInstructorProfile(newUserId, rollOrDept);
        }
    }

    public void updateUser(int userId, String username, String role, String rollOrDept, String program) throws SQLException, ServiceException {
        checkAdminPermission();

        authDAO.updateUsername(userId, username);
        if ("Student".equals(role)) {
            studentDAO.updateStudentProfile(userId, rollOrDept, program);
        } else if ("Instructor".equals(role)) {
            instructorDAO.updateInstructorProfile(userId, rollOrDept);
        }
    }

    public void resetUserPassword(String username, String newPlainPassword) throws SQLException, ServiceException {
        checkAdminPermission();

        int userId = authDAO.getUserIdByUsername(username);
        if (userId == -1) throw new ServiceException("User '" + username + "' not found.");
        String newHash = PasswordHasher.hashPassword(newPlainPassword);
        authDAO.updatePassword(userId, newHash);
    }

    public List<Student> getAllStudents() throws SQLException { return studentDAO.getAllStudents(); }
    public List<Instructor> getAllInstructors() throws SQLException { return instructorDAO.getAllInstructors(); }

    public void createCourse(String code, String title, int credits) throws SQLException, ServiceException {
        checkAdminPermission();
        courseDAO.createCourse(code, title, credits);
    }

    public void updateCourse(String code, String title, int credits) throws SQLException, ServiceException {
        checkAdminPermission();
        courseDAO.updateCourse(code, title, credits);
    }

    public void createSection(String courseCode, String instructorUsername, String dayTime, String room, int capacity) throws SQLException, ServiceException {
        checkAdminPermission();

        // 1. Resolve Course Code to Course ID
        int courseId = courseDAO.getCourseIdByCode(courseCode);
        if (courseId == -1) throw new ServiceException("Course '" + courseCode + "' not found.");

        // 2. Resolve Instructor Username to User ID
        int instructorId = authDAO.getUserIdByUsername(instructorUsername);
        if (instructorId == -1) throw new ServiceException("Instructor '" + instructorUsername + "' not found.");

        // 3. Create Section
        courseDAO.createSection(courseId, instructorId, dayTime, room, capacity, "Monsoon", 2025);
    }

    public void updateSection(int sectionId, String courseCode, String instructorUsername, String dayTime, String room, int capacity, String semester, int year) throws SQLException, ServiceException {
        checkAdminPermission();

        // 1. Resolve Course Code to Course ID
        int courseId = courseDAO.getCourseIdByCode(courseCode);
        if (courseId == -1) throw new ServiceException("Course '" + courseCode + "' not found.");

        // 2. Resolve Instructor Username to User ID
        int instructorId = authDAO.getUserIdByUsername(instructorUsername);
        if (instructorId == -1) throw new ServiceException("Instructor '" + instructorUsername + "' not found.");

        // 3. Update Section
        courseDAO.updateSection(sectionId, courseId, instructorId, dayTime, room, capacity, semester, year);
    }
}