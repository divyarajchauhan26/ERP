package edu.univ.erp.domain;

public class Section {
    // Fields from the 'sections' table
    private int sectionId;
    private int courseId;
    private int instructorId;
    private String dayTime;
    private String room;
    private int capacity;
    private String semester;
    private int year;

    // Extra fields from JOINs (for UI display)
    private String courseCode;
    private String courseTitle;
    private int credits;
    private String instructorName;

    // Main constructor
    public Section(int sectionId, int courseId, int instructorId, String dayTime, String room, int capacity, String semester, int year) {
        this.sectionId = sectionId;
        this.courseId = courseId;
        this.instructorId = instructorId;
        this.dayTime = dayTime;
        this.room = room;
        this.capacity = capacity;
        this.semester = semester;
        this.year = year;
    }

    // --- Getters ---
    public int getSectionId() { return sectionId; }
    public int getCourseId() { return courseId; }
    public int getInstructorId() { return instructorId; }
    public String getDayTime() { return dayTime; }
    public String getRoom() { return room; }
    public int getCapacity() { return capacity; }
    public String getSemester() { return semester; }
    public int getYear() { return year; }

    // --- Getters/Setters for extra UI fields ---
    public String getCourseCode() { return courseCode; }
    public void setCourseCode(String courseCode) { this.courseCode = courseCode; }

    public String getCourseTitle() { return courseTitle; }
    public void setCourseTitle(String courseTitle) { this.courseTitle = courseTitle; }

    public int getCredits() { return credits; }
    public void setCredits(int credits) { this.credits = credits; }

    public String getInstructorName() { return instructorName; }
    public void setInstructorName(String instructorName) { this.instructorName = instructorName; }
}