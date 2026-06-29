package edu.univ.erp.data;

import edu.univ.erp.domain.Enrollment;
import edu.univ.erp.domain.Section;
import edu.univ.erp.domain.Student;

import java.sql.SQLException;
import java.util.List;

public interface EnrollmentDAO {

    List<Enrollment> getEnrollmentsByStudentId(int studentId) throws SQLException;

    void addEnrollment(int studentId, int sectionId) throws SQLException;

    void deleteEnrollment(int enrollmentId) throws SQLException;

    List<Section> getEnrolledSectionsByStudentId(int studentId) throws SQLException;

    List<Student> getStudentsBySectionId(int sectionId) throws SQLException;

    int getEnrollmentCount(int sectionId) throws SQLException;

    int getSectionIdByEnrollmentId(int enrollmentId) throws SQLException;

    boolean isStudentEnrolledInCourse(int studentId, int courseId) throws SQLException;
}