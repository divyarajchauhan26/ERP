package edu.univ.erp.service;

import edu.univ.erp.data.CourseDAO;
import edu.univ.erp.data.CourseDAOImpl;
import edu.univ.erp.domain.Section; // Make sure to import your Section class

import java.sql.SQLException;
import java.util.List;

public class CatalogService {

    private CourseDAO courseDAO;

    public CatalogService() {
        this.courseDAO = new CourseDAOImpl(); // We'll need to code this DAO
    }

    /**
     * Gets all sections available for registration.
     * In a real app, you'd filter by semester.
     */
    public List<Section> getAllSections() throws SQLException {
        // For now, this is a simple call.
        // We need to implement getAllSections in CourseDAOImpl next.
        return courseDAO.getAllSections();
    }
}