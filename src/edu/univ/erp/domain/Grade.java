package edu.univ.erp.domain;

public class Grade {
    private int gradeId;
    private int enrollmentId;
    private String component; // e.g., "Quiz"
    private double score;     // Obtained marks
    private double maxMarks;  // Total marks possible (e.g., 20, 50, 100)
    private double weightage; // Percentage contribution (e.g., 10, 30)
    private String finalGrade;

    public Grade(int gradeId, int enrollmentId, String component, double score, double maxMarks, double weightage, String finalGrade) {
        this.gradeId = gradeId;
        this.enrollmentId = enrollmentId;
        this.component = component;
        this.score = score;
        this.maxMarks = maxMarks;
        this.weightage = weightage;
        this.finalGrade = finalGrade;
    }

    // --- Getters ---
    public int getGradeId() { return gradeId; }
    public int getEnrollmentId() { return enrollmentId; }
    public String getComponent() { return component; }
    public double getScore() { return score; }
    public double getMaxMarks() { return maxMarks; }
    public double getWeightage() { return weightage; }
    public String getFinalGrade() { return finalGrade; }
}