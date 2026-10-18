package com.vuongquangminh.eventmanagerfirebase;
import java.io.Serializable;

/**
 * Event Model Class
 * Represents the Event entity within the Event Management system.
 */
public class Event implements Serializable {
    private int id;
    private String name;
    private String location;
    private String date;
    private String time;
    private boolean requiresRiskAssessment;
    private String reporter;
    private String description;

    // Constructor used when retrieving data from the SQLite database (have ID)
    public Event(int id, String name, String location, String date, String time,
                 boolean requiresRiskAssessment, String reporter, String description) {
        this.id = id;
        this.name = name;
        this.location = location;
        this.date = date;
        this.time = time;
        this.requiresRiskAssessment = requiresRiskAssessment;
        this.reporter = reporter;
        this.description = description;
    }

    // Constructor used when initializing a new object before saving it to SQLite (No ID)
    public Event(String name, String location, String date, String time,
                 boolean requiresRiskAssessment, String reporter, String description) {
        this.id = -1;
        this.name = name;
        this.location = location;
        this.date = date;
        this.time = time;
        this.requiresRiskAssessment = requiresRiskAssessment;
        this.reporter = reporter;
        this.description = description;
    }

    // Getters and Setters for ID
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    // Getters và Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public boolean isRequiresRiskAssessment() {
        return requiresRiskAssessment;
    }

    public void setRequiresRiskAssessment(boolean requiresRiskAssessment) {
        this.requiresRiskAssessment = requiresRiskAssessment;
    }

    public String getReporter() {
        return reporter;
    }

    public void setReporter(String reporter) {
        this.reporter = reporter;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    // Supports data printing/logging for debugging purposes.
    @Override
    public String toString() {
        return "Event{" +
                "id=" + id +
                "name='" + name + '\'' +
                ", location='" + location + '\'' +
                ", date='" + date + '\'' +
                ", time='" + time + '\'' +
                ", requiresRiskAssessment=" + requiresRiskAssessment +
                ", reporter='" + reporter + '\'' +
                ", description='" + description + '\'' +
                '}';
    }
}
