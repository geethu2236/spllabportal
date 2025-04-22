package com.example.spllabportal;

public class Achievement {
    private String name;
    private String rollNumber;
    private String year;
    private String department;
    private String specialLab;
    private String eventName;
    private String projectTitle;
    private String eventDate;
    private String eventType;
    private String eventMode;
    private String achievementType;
    private String status;
    private String proofLink;

    // Default constructor (required for Firebase)
    public Achievement() {
    }

    // Parameterized Constructor
    public Achievement(String name, String rollNumber, String year, String department, String specialLab,
                       String eventName, String projectTitle, String eventDate, String eventType,
                       String eventMode, String achievementType, String status, String proofLink) {
        this.name = name;
        this.rollNumber = rollNumber;
        this.year = year;
        this.department = department;
        this.specialLab = specialLab;
        this.eventName = eventName;
        this.projectTitle = projectTitle;
        this.eventDate = eventDate;
        this.eventType = eventType;
        this.eventMode = eventMode;
        this.achievementType = achievementType;
        this.status = status;
        this.proofLink = proofLink;

    }

    // Getters and Setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getRollNumber() { return rollNumber; }
    public void setRollNumber(String rollNumber) { this.rollNumber = rollNumber; }

    public String getYear() { return year; }
    public void setYear(String year) { this.year = year; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public String getSpecialLab() { return specialLab; }
    public void setSpecialLab(String specialLab) { this.specialLab = specialLab; }

    public String getEventName() { return eventName; }
    public void setEventName(String eventName) { this.eventName = eventName; }

    public String getProjectTitle() { return projectTitle; }
    public void setProjectTitle(String projectTitle) { this.projectTitle = projectTitle; }

    public String getEventDate() { return eventDate; }
    public void setEventDate(String eventDate) { this.eventDate = eventDate; }

    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }

    public String getEventMode() { return eventMode; }
    public void setEventMode(String eventMode) { this.eventMode = eventMode; }

    public String getAchievementType() { return achievementType; }
    public void setAchievementType(String achievementType) { this.achievementType = achievementType; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getProofLink() { return proofLink; }
    public void setProofLink(String proofLink) { this.proofLink = proofLink; }
}
