package edu.univ.erp.data;

import edu.univ.erp.domain.Student;
import java.sql.SQLException;
import java.util.List;

public interface StudentDAO {

    void createStudentProfile(int userId, String rollNo, String program) throws SQLException;

    void updateStudentProfile(int userId, String rollNo, String program) throws SQLException;

    List<Student> getAllStudents() throws SQLException;

}