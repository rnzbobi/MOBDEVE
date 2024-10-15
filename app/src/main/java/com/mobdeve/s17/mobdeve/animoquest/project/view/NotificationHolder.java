package com.mobdeve.s17.mobdeve.animoquest.project.view;

public class NotificationHolder {
    private String title;
    private String subject;
    private String message;
    private String timestamp;
    private int profileImageResId;
    private int posterImageResId;

    public NotificationHolder(String title, String subject, String message, String timestamp, int profileImageResId, int posterImageResId) {
        this.title = title;
        this.subject = subject;
        this.message = message;
        this.timestamp = timestamp;
        this.profileImageResId = profileImageResId;
        this.posterImageResId = posterImageResId;
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

    public int getProfileImageResId() {
        return profileImageResId;
    }

    public int getPosterImageResId() {
        return posterImageResId;
    }
}
