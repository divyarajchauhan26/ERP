package edu.univ.erp.access;

import edu.univ.erp.data.SettingsDAO;
import edu.univ.erp.data.SettingsDAOImpl;
import edu.univ.erp.data.CourseDAO;
import edu.univ.erp.data.CourseDAOImpl;
import edu.univ.erp.auth.UserSession;
import edu.univ.erp.domain.Section;

import java.sql.SQLException;
import java.util.List;

/**
 * A central service to check permissions and system-wide flags
 * like Maintenance Mode.
 */
public class AccessControlService {

    private SettingsDAO settingsDAO;
    private CourseDAO courseDAO;

    public AccessControlService() {
        this.settingsDAO = new SettingsDAOImpl();
        this.courseDAO = new CourseDAOImpl();
    }

    /**
     * Checks if the system is in read-only maintenance mode.
     * @return true if maintenance is ON, false otherwise.
     * @throws SQLException
     */
    public boolean isMaintenanceModeOn() throws SQLException {
        return settingsDAO.isMaintenanceModeOn();
    }

    public boolean isCurrentUserAdmin() {
        if (!UserSession.getInstance().isLoggedIn()) return false;
        return "Admin".equals(UserSession.getInstance().getCurrentUser().getRole());
    }

    /**
     * Checks if the given instructor teaches the specified section.
     * @param instructorId The user ID of the instructor.
     * @param sectionId The ID of the section to check.
     * @return true if the instructor is assigned to the section, false otherwise.
     */
    public boolean isInstructorForSection(int instructorId, int sectionId) throws SQLException {
        // Optimization: Only check sections if logged in and it's an Instructor role
        if (!UserSession.getInstance().isLoggedIn() ||
                !"Instructor".equals(UserSession.getInstance().getCurrentUser().getRole())) {
            return false;
        }

        List<Section> instructorSections = courseDAO.getSectionsByInstructorId(instructorId);
        for (Section sec : instructorSections) {
            if (sec.getSectionId() == sectionId) {
                return true;
            }
        }
        return false;
    }
}