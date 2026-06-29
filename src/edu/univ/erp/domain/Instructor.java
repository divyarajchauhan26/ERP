package edu.univ.erp.domain;

public class Instructor {
    private int userId; // This links to User.userId
    private String department;
    private String username; // <--- NEW FIELD

    public Instructor(int userId, String department) {
        this.userId = userId;
        this.department = department;
    }

    // --- Getters & Setters ---
    public int getUserId() { return userId; }
    public String getDepartment() { return department; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
}