package edu.univ.erp.service;

import edu.univ.erp.access.AccessControlService;
import edu.univ.erp.data.*;
import edu.univ.erp.domain.Enrollment;
import edu.univ.erp.domain.Grade;
import edu.univ.erp.domain.Section;
import edu.univ.erp.service.ServiceException;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class StudentService {

    private EnrollmentDAO enrollmentDAO;
    private GradeDAO gradeDAO;
    private AccessControlService accessControl;
    private SettingsDAO settingsDAO;
    private CourseDAO courseDAO;
    private StudentDAO studentDAO;

    public StudentService() {
        this.enrollmentDAO = new EnrollmentDAOImpl();
        this.gradeDAO = new GradeDAOImpl();
        this.accessControl = new AccessControlService();
        this.settingsDAO = new SettingsDAOImpl();
        this.courseDAO = new CourseDAOImpl();
        this.studentDAO = new StudentDAOImpl();
    }

    /**
     * Registers a student for a section.
     */
    public void registerForSection(int studentId, int sectionId) throws SQLException, ServiceException {
        // 1. Check Maintenance Mode
        if (accessControl.isMaintenanceModeOn()) {
            throw new ServiceException("Maintenance Mode is ON. Registration is temporarily disabled.");
        }

        // 2. Check Registration Deadline
        LocalDate deadline = settingsDAO.getRegistrationDeadline();
        LocalDate today = settingsDAO.getDatabaseCurrentDate();

        if (today.isAfter(deadline)) {
            throw new ServiceException("Registration closed on " + deadline + ". You cannot register anymore.");
        }

        // --- Get Section Details ---
        List<Section> allSections = courseDAO.getAllSections();
        Section targetSection = null;
        for (Section sec : allSections) {
            if (sec.getSectionId() == sectionId) {
                targetSection = sec;
                break;
            }
        }

        if (targetSection == null) {
            throw new ServiceException("Section not found.");
        }

        // 3. Check Capacity
        int capacity = targetSection.getCapacity();
        if (capacity <= 0) {
            throw new ServiceException("Section capacity is invalid.");
        }

        int currentEnrollment = enrollmentDAO.getEnrollmentCount(sectionId);

        if (currentEnrollment >= capacity) {
            throw new ServiceException("Section full. Cannot register.");
        }

        // 5. NEW CHECK: DUPLICATE COURSE ENROLLMENT (Cannot take two sections of same course)
        if (enrollmentDAO.isStudentEnrolledInCourse(studentId, targetSection.getCourseId())) {
            throw new ServiceException("You are already enrolled in a section of this course (" + targetSection.getCourseCode() + ").");
        }
        // --- END NEW CHECK ---

        // 6. Attempt Registration
        try {
            enrollmentDAO.addEnrollment(studentId, sectionId);
        } catch (SQLException e) {
            if (e.getErrorCode() == 1062) {
                throw new ServiceException("You are already registered for this specific section.");
            }
            throw new ServiceException("Database error. Could not register. Error Code: " + e.getErrorCode());
        }
    }

    /**
     * Drops a student from a section.
     */
    public void dropSection(int enrollmentId) throws SQLException, ServiceException {
        // 1. Check Maintenance Mode
        if (accessControl.isMaintenanceModeOn()) {
            throw new ServiceException("Maintenance Mode is ON. Dropping courses is temporarily disabled.");
        }

        // 2. CHECK: DROP DEADLINE
        LocalDate deadline = settingsDAO.getDropDeadline();
        LocalDate today = settingsDAO.getDatabaseCurrentDate();

        if (today.isAfter(deadline)) {
            throw new ServiceException("Drop period ended on " + deadline + ". You cannot drop this course.");
        }

        // 3. Proceed with Drop
        enrollmentDAO.deleteEnrollment(enrollmentId);
    }

    public List<Enrollment> getMyEnrollments(int studentId) throws SQLException {
        return enrollmentDAO.getEnrollmentsByStudentId(studentId);
    }

    public List<Section> getMyTimetable(int studentId) throws SQLException {
        return enrollmentDAO.getEnrolledSectionsByStudentId(studentId);
    }

    public List<Grade> getGrades(int enrollmentId) throws SQLException {
        return gradeDAO.getGradesForEnrollment(enrollmentId);
    }
}