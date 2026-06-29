package edu.univ.erp.data;

import edu.univ.erp.domain.Enrollment;
import edu.univ.erp.domain.Section;
import edu.univ.erp.domain.Student;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class EnrollmentDAOImpl implements EnrollmentDAO {

    @Override
    public List<Enrollment> getEnrollmentsByStudentId(int studentId) throws SQLException {
        List<Enrollment> enrollments = new ArrayList<>();
        String sql = "SELECT * FROM enrollments WHERE student_id = ?";

        try (Connection conn = DatabaseConnector.getErpConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, studentId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    enrollments.add(new Enrollment(
                            rs.getInt("enrollment_id"),
                            rs.getInt("student_id"),
                            rs.getInt("section_id"),
                            rs.getString("status")
                    ));
                }
            }
        }
        return enrollments;
    }

    @Override
    public void addEnrollment(int studentId, int sectionId) throws SQLException {
        String sql = "INSERT INTO enrollments (student_id, section_id, status) VALUES (?, ?, 'Enrolled')";

        try (Connection conn = DatabaseConnector.getErpConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, studentId);
            stmt.setInt(2, sectionId);
            stmt.executeUpdate();
        }
    }

    @Override
    public void deleteEnrollment(int enrollmentId) throws SQLException {
        String sql = "DELETE FROM enrollments WHERE enrollment_id = ?";

        try (Connection conn = DatabaseConnector.getErpConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, enrollmentId);
            stmt.executeUpdate();
        }
    }

    @Override
    public List<Section> getEnrolledSectionsByStudentId(int studentId) throws SQLException {
        List<Section> sections = new ArrayList<>();

        String sql = "SELECT s.*, c.code, c.title, c.credits, ua.username as instructor_name " +
                "FROM enrollments e " +
                "JOIN sections s ON e.section_id = s.section_id " +
                "JOIN courses c ON s.course_id = c.course_id " +
                "LEFT JOIN instructors i ON s.instructor_id = i.user_id " +
                "LEFT JOIN university_auth_db.users_auth ua ON i.user_id = ua.user_id " +
                "WHERE e.student_id = ? AND e.status = 'Enrolled'";

        try (Connection conn = DatabaseConnector.getErpConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, studentId);

            try (ResultSet rs = stmt.executeQuery()) {
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
        }
        return sections;
    }

    @Override
    public List<Student> getStudentsBySectionId(int sectionId) throws SQLException {
        List<Student> students = new ArrayList<>();

        String sql = "SELECT s.*, u.username FROM students s " +
                "JOIN enrollments e ON s.user_id = e.student_id " +
                "JOIN university_auth_db.users_auth u ON s.user_id = u.user_id " +
                "WHERE e.section_id = ? AND e.status = 'Enrolled'";

        try (Connection conn = DatabaseConnector.getErpConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, sectionId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Student student = new Student(
                            rs.getInt("user_id"),
                            rs.getString("roll_no"),
                            rs.getString("program"),
                            rs.getInt("year")
                    );
                    student.setUsername(rs.getString("username"));
                    students.add(student);
                }
            }
        }
        return students;
    }

    @Override
    public int getEnrollmentCount(int sectionId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM enrollments WHERE section_id = ? AND status = 'Enrolled'";
        try (Connection conn = DatabaseConnector.getErpConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, sectionId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        return 0;
    }

    @Override
    public int getSectionIdByEnrollmentId(int enrollmentId) throws SQLException {
        String sql = "SELECT section_id FROM enrollments WHERE enrollment_id = ?";
        try (Connection conn = DatabaseConnector.getErpConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, enrollmentId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return rs.getInt("section_id");
            }
        }
        return -1;
    }

    @Override
    public boolean isStudentEnrolledInCourse(int studentId, int courseId) throws SQLException {
        // Query enrollments joined to sections to find the course ID
        String sql = "SELECT COUNT(*) FROM enrollments e " +
                "JOIN sections s ON e.section_id = s.section_id " +
                "WHERE e.student_id = ? AND s.course_id = ? AND e.status = 'Enrolled'";

        try (Connection conn = DatabaseConnector.getErpConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, studentId);
            stmt.setInt(2, courseId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }
}