package edu.univ.erp.data;

import edu.univ.erp.domain.Grade;
import java.sql.SQLException;
import java.util.List;

public interface GradeDAO {

    List<Grade> getGradesForEnrollment(int enrollmentId) throws SQLException;

    void saveOrUpdateGrade(int enrollmentId, String component, double score, double maxMarks, double weightage) throws SQLException;

    /**
     * Updates the final grade for a specific enrollment.
     */
    void updateFinalGrade(int enrollmentId, String finalGrade) throws SQLException;
}