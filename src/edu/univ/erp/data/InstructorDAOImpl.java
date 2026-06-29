package edu.univ.erp.data;

import edu.univ.erp.domain.Instructor;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class InstructorDAOImpl implements InstructorDAO {

    @Override
    public void createInstructorProfile(int userId, String department) throws SQLException {
        String sql = "INSERT INTO instructors (user_id, department) VALUES (?, ?)";
        try (Connection conn = DatabaseConnector.getErpConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setString(2, department);
            stmt.executeUpdate();
        }
    }

    // --- THIS WAS MISSING ---
    @Override
    public void updateInstructorProfile(int userId, String department) throws SQLException {
        String sql = "UPDATE instructors SET department = ? WHERE user_id = ?";
        try (Connection conn = DatabaseConnector.getErpConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, department);
            stmt.setInt(2, userId);
            stmt.executeUpdate();
        }
    }

    @Override
    public List<Instructor> getAllInstructors() throws SQLException {
        List<Instructor> instructors = new ArrayList<>();
        String sql = "SELECT i.*, u.username FROM instructors i " +
                "JOIN university_auth_db.users_auth u ON i.user_id = u.user_id " +
                "ORDER BY u.username ASC";

        try (Connection conn = DatabaseConnector.getErpConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Instructor inst = new Instructor(
                        rs.getInt("user_id"),
                        rs.getString("department")
                );
                inst.setUsername(rs.getString("username"));
                instructors.add(inst);
            }
        }
        return instructors;
    }
}