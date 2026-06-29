package edu.univ.erp.domain;

public class Enrollment {
    private int enrollmentId;
    private int studentId; // Links to Student.userId
    private int sectionId; // Links to Section.sectionId
    private String status; // e.g., "Enrolled", "Dropped"

    public Enrollment(int enrollmentId, int studentId, int sectionId, String status) {
        this.enrollmentId = enrollmentId;
        this.studentId = studentId;
        this.sectionId = sectionId;
        this.status = status;
    }

    // --- Getters ---
    public int getEnrollmentId() { return enrollmentId; }
    public int getStudentId() { return studentId; }
    public int getSectionId() { return sectionId; }
    public String getStatus() { return status; }
}