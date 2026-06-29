package edu.univ.erp.domain;

public class Student {
    private int userId;
    private String rollNo;
    private String program;
    private int year;
    private String username; // <--- NEW FIELD

    public Student(int userId, String rollNo, String program, int year) {
        this.userId = userId;
        this.rollNo = rollNo;
        this.program = program;
        this.year = year;
    }

    // --- Getters ---
    public int getUserId() { return userId; }
    public String getRollNo() { return rollNo; }
    public String getProgram() { return program; }
    public int getYear() { return year; }

    // --- New Getter & Setter ---
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
}