package com.payment.payment;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.LocalDateTime;

@Entity
public class Log {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String action; // e.g., "Payment created", "Payment deleted"
    private String details; // e.g., "Deleted payment with ID: 123"
    private String user;
    private String sourceIp;
    private LocalDateTime timestamp;

    public Log() {
        this.timestamp = LocalDateTime.now();
    }

    // This is the new constructor that fixes the error
    public Log(String action, String details, String user, String sourceIp) {
        this();
        this.action = action;
        this.details = details;
        this.user = user;
        this.sourceIp = sourceIp;
    }

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }
    public String getDetails() { return details; }
    public void setDetails(String details) { this.details = details; }
    public String getUser() { return user; }
    public void setUser(String user) { this.user = user; }
    public String getSourceIp() { return sourceIp; }
    public void setSourceIp(String sourceIp) { this.sourceIp = sourceIp; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}