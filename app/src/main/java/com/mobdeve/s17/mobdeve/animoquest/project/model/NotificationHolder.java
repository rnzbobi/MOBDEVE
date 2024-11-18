package com.mobdeve.s17.mobdeve.animoquest.project.model;

public class NotificationHolder {
    private String title;
    private String subject;
    private String message;
    private String timestamp;

    public NotificationHolder(String title, String subject, String message, String timestamp) {
        this.title = title;
        this.subject = subject;
        this.message = message;
        this.timestamp = timestamp;
    }

    // Getters
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
}
