package edu.univ.erp.data;

import edu.univ.erp.domain.Student;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class StudentDAOImpl implements StudentDAO {

    @Override
    public void createStudentProfile(int userId, String rollNo, String program) throws SQLException {
        String sql = "INSERT INTO students (user_id, roll_no, program, year) VALUES (?, ?, ?, 1)";
        try (Connection conn = DatabaseConnector.getErpConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setString(2, rollNo);
            stmt.setString(3, program);
            stmt.executeUpdate();
        }
    }

    @Override
    public void updateStudentProfile(int userId, String rollNo, String program) throws SQLException {
        String sql = "UPDATE students SET roll_no = ?, program = ? WHERE user_id = ?";
        try (Connection conn = DatabaseConnector.getErpConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, rollNo);
            stmt.setString(2, program);
            stmt.setInt(3, userId);
            stmt.executeUpdate();
        }
    }

    @Override
    public List<Student> getAllStudents() throws SQLException {
        List<Student> students = new ArrayList<>();
        String sql = "SELECT s.*, u.username FROM students s " +
                "JOIN university_auth_db.users_auth u ON s.user_id = u.user_id " +
                "ORDER BY s.roll_no ASC";

        try (Connection conn = DatabaseConnector.getErpConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Student s = new Student(
                        rs.getInt("user_id"),
                        rs.getString("roll_no"),
                        rs.getString("program"),
                        rs.getInt("year")
                );
                s.setUsername(rs.getString("username"));
                students.add(s);
            }
        }
        return students;
    }
}

