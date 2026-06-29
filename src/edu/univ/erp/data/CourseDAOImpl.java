package edu.univ.erp.data;

import edu.univ.erp.domain.Course;
import edu.univ.erp.domain.Section;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CourseDAOImpl implements CourseDAO {

    @Override
    public List<Section> getAllSections() throws SQLException {
        return getSectionsByQuery(
                "SELECT s.*, c.code, c.title, c.credits, ua.username as instructor_name " +
                        "FROM sections s " +
                        "JOIN courses c ON s.course_id = c.course_id " +
                        "LEFT JOIN instructors i ON s.instructor_id = i.user_id " +
                        "LEFT JOIN university_auth_db.users_auth ua ON i.user_id = ua.user_id " +
                        "ORDER BY c.code ASC"
        );
    }

    @Override
    public List<Section> getSectionsByInstructorId(int instructorId) throws SQLException {
        String sql = "SELECT s.*, c.code, c.title, c.credits, ua.username as instructor_name " +
                "FROM sections s " +
                "JOIN courses c ON s.course_id = c.course_id " +
                "LEFT JOIN instructors i ON s.instructor_id = i.user_id " +
                "LEFT JOIN university_auth_db.users_auth ua ON i.user_id = ua.user_id " +
                "WHERE s.instructor_id = " + instructorId +
                " ORDER BY c.code ASC";
        return getSectionsByQuery(sql);
    }

    private List<Section> getSectionsByQuery(String sql) throws SQLException {
        List<Section> sections = new ArrayList<>();
        try (Connection conn = DatabaseConnector.getErpConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Section section = new Section(
                        rs.getInt("section_id"),
                        rs.getInt("course_id"),
                        rs.getInt("instructor_id"),
                        rs.getString("day_time"),
                        rs.getString("room"),
                        rs.getInt("capacity"),
                        rs.getString("semester"),
                        rs.getInt("year")
                );
                section.setCourseCode(rs.getString("code"));
                section.setCourseTitle(rs.getString("title"));
                section.setCredits(rs.getInt("credits"));
                section.setInstructorName(rs.getString("instructor_name"));
                sections.add(section);
            }
        }
        return sections;
    }

    @Override
    public List<Course> getAllCourses() throws SQLException {
        List<Course> courses = new ArrayList<>();
        String sql = "SELECT course_id, code, title, credits FROM courses ORDER BY code ASC";
        try (Connection conn = DatabaseConnector.getErpConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                courses.add(new Course(
                        rs.getInt("course_id"),
                        rs.getString("code"),
                        rs.getString("title"),
                        rs.getInt("credits")
                ));
            }
        }
        return courses;
    }

    @Override
    public int getCourseIdByCode(String code) throws SQLException {
        String sql = "SELECT course_id FROM courses WHERE code = ?";
        try (Connection conn = DatabaseConnector.getErpConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, code);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return rs.getInt("course_id");
            }
        }
        return -1;
    }

    @Override
    public void createCourse(String code, String title, int credits) throws SQLException {
        String sql = "INSERT INTO courses (code, title, credits) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseConnector.getErpConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, code);
            stmt.setString(2, title);
            stmt.setInt(3, credits);
            stmt.executeUpdate();
        }
    }

    @Override
    public void updateCourse(String code, String title, int credits) throws SQLException {
        String sql = "UPDATE courses SET title = ?, credits = ? WHERE code = ?";
        try (Connection conn = DatabaseConnector.getErpConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, title);
            stmt.setInt(2, credits);
            stmt.setString(3, code);
            stmt.executeUpdate();
        }
    }

    @Override
    public void createSection(int courseId, int instructorId, String dayTime, String room, int capacity, String semester, int year) throws SQLException {
        String sql = "INSERT INTO sections (course_id, instructor_id, day_time, room, capacity, semester, year) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnector.getErpConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, courseId);
            stmt.setInt(2, instructorId);
            stmt.setString(3, dayTime);
            stmt.setString(4, room);
            stmt.setInt(5, capacity);
            stmt.setString(6, semester);
            stmt.setInt(7, year);
            stmt.executeUpdate();
        }
    }

    @Override
    public void updateSection(int sectionId, int courseId, int instructorId, String dayTime, String room, int capacity, String semester, int year) throws SQLException {
        String sql = "UPDATE sections SET course_id=?, instructor_id=?, day_time=?, room=?, capacity=?, semester=?, year=? WHERE section_id=?";
        try (Connection conn = DatabaseConnector.getErpConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, courseId);
            stmt.setInt(2, instructorId);
            stmt.setString(3, dayTime);
            stmt.setString(4, room);
            stmt.setInt(5, capacity);
            stmt.setString(6, semester);
            stmt.setInt(7, year);
            stmt.setInt(8, sectionId);
            stmt.executeUpdate();
        }
    }

    @Override
    public void deleteSection(int sectionId) throws SQLException {
        String sql = "DELETE FROM sections WHERE section_id = ?";
        try (Connection conn = DatabaseConnector.getErpConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, sectionId);
            stmt.executeUpdate();
        }
    }

    @Override
    public void deleteCourse(String courseCode) throws SQLException {
        String sql = "DELETE FROM courses WHERE code = ?";
        try (Connection conn = DatabaseConnector.getErpConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, courseCode);
            stmt.executeUpdate();
        }
    }
}