package com.mobdeve.s17.mobdeve.animoquest.project.model;

public class NotificationHolder {
    private String id;
    private String title;
    private String subject;
    private String message;
    private String timestamp;
    private Boolean isRead;

    public NotificationHolder(String id, String title, String subject, String message, String timestamp, Boolean isRead) {
        this.id = id;
        this.title = title;
        this.subject = subject;
        this.message = message;
        this.timestamp = timestamp;
        this.isRead = isRead;
    }

    // Getters
    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getSubject() {
        return subject;
    }

    public String getMessage() {
        return message;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public Boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        isRead = read;
    }
}
