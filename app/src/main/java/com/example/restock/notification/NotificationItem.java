package com.example.restock.notification;

// NotificationItem.java
// Data class holding notification details (title, message, time)
public class NotificationItem {
    String title;
    String message;
    String time; // timestamp

    public NotificationItem(String title, String message, String time) {
        this.title = title;
        this.message = message;
        this.time = time;
    }

    // Getters
    public String getTitle() { return title; }
    public String getMessage() { return message; }
    public String getTime() { return time; }
}

// -------- Documentation -------- //
// NotificationItem.java
// Data model class representing a single notification with a title, message, and timestamp