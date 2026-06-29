package edu.univ.erp.service;

import edu.univ.erp.access.AccessControlService;
import edu.univ.erp.data.*;
import edu.univ.erp.domain.Enrollment;
import edu.univ.erp.domain.Grade;
import edu.univ.erp.domain.Section;
import edu.univ.erp.domain.Student;
import edu.univ.erp.service.ServiceException;
import edu.univ.erp.auth.UserSession;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InstructorService {

    private CourseDAO courseDAO;
    private EnrollmentDAO enrollmentDAO;
    private GradeDAO gradeDAO;
    private AccessControlService accessControl;

    public InstructorService() {
        this.courseDAO = new CourseDAOImpl();
        this.enrollmentDAO = new EnrollmentDAOImpl();
        this.gradeDAO = new GradeDAOImpl();
        this.accessControl = new AccessControlService();
    }

    public List<Section> getMySections(int instructorId) throws SQLException {
        return courseDAO.getSectionsByInstructorId(instructorId);
    }

    public List<Student> getStudentsBySection(int sectionId) throws SQLException {
        return enrollmentDAO.getStudentsBySectionId(sectionId);
    }
    public List<Map<String, Object>> getFullGradeRosterForExport(int sectionId) throws SQLException, ServiceException {

        // ENFORCE OWNERSHIP: Check if current instructor teaches this section
        int instructorId = UserSession.getInstance().getCurrentUser().getUserId();
        if (!accessControl.isInstructorForSection(instructorId, sectionId)) {
            throw new ServiceException("Permission Denied: You are not assigned to teach this section.");
        }

        List<Student> students = enrollmentDAO.getStudentsBySectionId(sectionId);
        List<Map<String, Object>> roster = new ArrayList<>();

        // Track unique grade components to build dynamic headers
        Map<String, Double> componentMaxMarks = new HashMap<>();

        for (Student s : students) {
            Map<String, Object> row = new HashMap<>();
            row.put("Roll No", s.getRollNo());
            row.put("Student Name", s.getUsername());

            // Find Enrollment ID
            List<Enrollment> enrs = enrollmentDAO.getEnrollmentsByStudentId(s.getUserId());
            int enrollmentId = -1;
            for (Enrollment e : enrs) {
                if (e.getSectionId() == sectionId) {
                    enrollmentId = e.getEnrollmentId();
                    break;
                }
            }

            if (enrollmentId != -1) {
                List<Grade> grades = gradeDAO.getGradesForEnrollment(enrollmentId);
                double totalEarnedWeight = 0.0;
                double totalPossibleWeight = 0.0;

                for (Grade g : grades) {
                    String componentName = g.getComponent();

                    // Add score in the format ComponentName (Score/MaxMarks)
                    row.put(componentName + " (Score)", g.getScore());
                    row.put(componentName + " (Max)", g.getMaxMarks());
                    row.put(componentName + " (Weight)", g.getWeightage());

                    // Track max marks for final calculation consistency
                    componentMaxMarks.put(componentName, g.getMaxMarks());

                    // Calculate weighted total for CSV display
                    if (g.getMaxMarks() > 0) {
                        totalEarnedWeight += (g.getScore() / g.getMaxMarks()) * g.getWeightage();
                        totalPossibleWeight += g.getWeightage();
                    }

                    // Save Final Grade status
                    if (g.getFinalGrade() != null && !g.getFinalGrade().isEmpty()) {
                        row.put("Final Grade", g.getFinalGrade());
                    }
                }

                // Add weighted total score
                row.put("Weighted Total", String.format("%.2f", totalEarnedWeight));
                row.put("Max Weight Possible", totalPossibleWeight);
            }
            roster.add(row);
        }
        return roster;
    }

    public void submitGrade(int enrollmentId, String component, double score, double maxMarks, double weightage) throws SQLException, ServiceException {
        if (accessControl.isMaintenanceModeOn()) {
            throw new ServiceException("Maintenance Mode is ON. Submitting grades is temporarily disabled.");
        }

        // 1. Get Section ID associated with the Enrollment
        int sectionId = enrollmentDAO.getSectionIdByEnrollmentId(enrollmentId);
        if (sectionId == -1) {
            throw new ServiceException("Enrollment not found.");
        }

        // 2. ENFORCE OWNERSHIP: Check if current instructor teaches this section
        int instructorId = UserSession.getInstance().getCurrentUser().getUserId();
        if (!accessControl.isInstructorForSection(instructorId, sectionId)) {
            throw new ServiceException("Permission Denied: You are not assigned to teach this section.");
        }

        gradeDAO.saveOrUpdateGrade(enrollmentId, component, score, maxMarks, weightage);
    }

    public List<Grade> getGradesForEnrollment(int enrollmentId) throws SQLException {
        return gradeDAO.getGradesForEnrollment(enrollmentId);
    }

    public void publishAllGrades(int sectionId, Map<String, Double> cutoffs) throws SQLException, ServiceException {
        // 1. Check Maintenance Mode
        if (accessControl.isMaintenanceModeOn()) {
            throw new ServiceException("Maintenance Mode is ON. Publishing grades is disabled.");
        }

        // 2. ENFORCE OWNERSHIP: Check if current instructor teaches this section
        int instructorId = UserSession.getInstance().getCurrentUser().getUserId();
        if (!accessControl.isInstructorForSection(instructorId, sectionId)) {
            throw new ServiceException("Permission Denied: You are not assigned to teach this section.");
        }

        // 3. Get all students in the section
        List<Student> students = enrollmentDAO.getStudentsBySectionId(sectionId);

        for (Student s : students) {
            // 4. Find Enrollment ID for this student in this section
            List<Enrollment> enrs = enrollmentDAO.getEnrollmentsByStudentId(s.getUserId());
            int enrollmentId = -1;
            for (Enrollment e : enrs) {
                if (e.getSectionId() == sectionId) {
                    enrollmentId = e.getEnrollmentId();
                    break;
                }
            }

            if (enrollmentId != -1) {
                // 5. Calculate Weighted Total
                List<Grade> grades = gradeDAO.getGradesForEnrollment(enrollmentId);
                double total = 0.0;
                for (Grade g : grades) {
                    if (g.getMaxMarks() > 0) {
                        double percentage = g.getScore() / g.getMaxMarks();
                        total += (percentage * g.getWeightage());
                    }
                }

                // 6. Determine Letter Grade based on Cutoffs
                String letter = "F";
                if (total >= cutoffs.get("A")) letter = "A";
                else if (total >= cutoffs.get("A-")) letter = "A-";
                else if (total >= cutoffs.get("B")) letter = "B";
                else if (total >= cutoffs.get("B-")) letter = "B-";
                else if (total >= cutoffs.get("C")) letter = "C";
                else if (total >= cutoffs.get("C-")) letter = "C-";
                else if (total >= cutoffs.get("D")) letter = "D";

                // 7. Save to DB
                gradeDAO.updateFinalGrade(enrollmentId, letter);
            }
        }
    }
}