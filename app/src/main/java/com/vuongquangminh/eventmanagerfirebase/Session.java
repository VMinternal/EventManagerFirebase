package com.vuongquangminh.eventmanagerfirebase;

import java.io.Serializable;

/**
 * Session Model Class
 * Represents a session belonging to a specific event within the Event Management system.
 */
public class Session implements Serializable {
    private int id;
    private int eventId; // Foreign key linked to Event
    private String sessionName;
    private String speaker;
    private String startTime;
    private String endTime;
    private String description;

    // Constructor used when retrieving data from SQLite (includes an ID)
    public Session(int id, int eventId, String sessionName, String speaker, String startTime, String endTime, String description) {
        this.id = id;
        this.eventId = eventId;
        this.sessionName = sessionName;
        this.speaker = speaker;
        this.startTime = startTime;
        this.endTime = endTime;
        this.description = description;
    }

    // Constructor dùng khi tạo mới session (chưa có ID)
    public Session(int eventId, String sessionName, String speaker, String startTime, String endTime, String description) {
        this.id = -1;
        this.eventId = eventId;
        this.sessionName = sessionName;
        this.speaker = speaker;
        this.startTime = startTime;
        this.endTime = endTime;
        this.description = description;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getEventId() {
        return eventId;
    }

    public void setEventId(int eventId) {
        this.eventId = eventId;
    }

    public String getSessionName() {
        return sessionName;
    }

    public void setSessionName(String sessionName) {
        this.sessionName = sessionName;
    }

    public String getSpeaker() {
        return speaker;
    }

    public void setSpeaker(String speaker) {
        this.speaker = speaker;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return "Session{" +
                "id=" + id +
                ", eventId=" + eventId +
                ", sessionName='" + sessionName + '\'' +
                ", speaker='" + speaker + '\'' +
                ", startTime='" + startTime + '\'' +
                ", endTime='" + endTime + '\'' +
                ", description='" + description + '\'' +
                '}';
    }
}