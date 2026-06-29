package edu.univ.erp.domain;

public class User {
    private int userId;
    private String username;
    private String role; // "Student", "Instructor", "Admin"

    public User(int userId, String username, String role) {
        this.userId = userId;
        this.username = username;
        this.role = role;
    }

    // --- Getters ---
    public int getUserId() { return userId; }
    public String getUsername() { return username; }
    public String getRole() { return role; }
}