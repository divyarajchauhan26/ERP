package edu.univ.erp.data;
import edu.univ.erp.domain.Instructor;
import java.sql.SQLException;
import java.util.List;

public interface InstructorDAO {
    void createInstructorProfile(int userId, String department) throws SQLException;

    // --- THIS WAS MISSING ---
    void updateInstructorProfile(int userId, String department) throws SQLException;

    List<Instructor> getAllInstructors() throws SQLException;
}