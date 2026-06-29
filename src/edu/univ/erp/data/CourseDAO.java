package edu.univ.erp.data;

import edu.univ.erp.domain.Course;
import edu.univ.erp.domain.Section;
import java.sql.SQLException;
import java.util.List;

public interface CourseDAO {

    List<Section> getAllSections() throws SQLException;

    List<Section> getSectionsByInstructorId(int instructorId) throws SQLException;

    List<Course> getAllCourses() throws SQLException;

    int getCourseIdByCode(String code) throws SQLException;

    void createCourse(String code, String title, int credits) throws SQLException;

    void updateCourse(String code, String title, int credits) throws SQLException;

    void createSection(int courseId, int instructorId, String dayTime, String room, int capacity, String semester, int year) throws SQLException;

    void updateSection(int sectionId, int courseId, int instructorId, String dayTime, String room, int capacity, String semester, int year) throws SQLException;

    void deleteSection(int sectionId) throws SQLException;

    // --- NEW METHOD ---
    void deleteCourse(String courseCode) throws SQLException;
}