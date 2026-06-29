package edu.univ.erp.data;

import edu.univ.erp.domain.Grade;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class GradeDAOImpl implements GradeDAO {

    @Override
    public List<Grade> getGradesForEnrollment(int enrollmentId) throws SQLException {
        List<Grade> grades = new ArrayList<>();
        String sql = "SELECT * FROM grades WHERE enrollment_id = ?";

        try (Connection conn = DatabaseConnector.getErpConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, enrollmentId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    grades.add(new Grade(
                            rs.getInt("grade_id"),
                            rs.getInt("enrollment_id"),
                            rs.getString("component"),
                            rs.getDouble("score"),
                            rs.getDouble("max_marks"),
                            rs.getDouble("weightage"),
                            rs.getString("final_grade")
                    ));
                }
            }
        }
        return grades;
    }

    @Override
    public void saveOrUpdateGrade(int enrollmentId, String component, double score, double maxMarks, double weightage) throws SQLException {
        // Insert or Update all fields including max_marks and weightage
        String sql = "INSERT INTO grades (enrollment_id, component, score, max_marks, weightage) VALUES (?, ?, ?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE score = VALUES(score), max_marks = VALUES(max_marks), weightage = VALUES(weightage)";

        try (Connection conn = DatabaseConnector.getErpConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, enrollmentId);
            stmt.setString(2, component);
            stmt.setDouble(3, score);
            stmt.setDouble(4, maxMarks);
            stmt.setDouble(5, weightage);
            stmt.executeUpdate();
        }
    }

    @Override
    public void updateFinalGrade(int enrollmentId, String finalGrade) throws SQLException {
        // Update ALL grade rows for this student with the final grade
        // (In this schema design, final_grade is stored on every row for convenience)
        String sql = "UPDATE grades SET final_grade = ? WHERE enrollment_id = ?";

        try (Connection conn = DatabaseConnector.getErpConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, finalGrade);
            stmt.setInt(2, enrollmentId);
            stmt.executeUpdate();
        }
    }
}