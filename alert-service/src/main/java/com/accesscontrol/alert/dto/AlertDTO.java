package com.accesscontrol.alert.dto;

import com.accesscontrol.alert.model.Alert;
import java.time.LocalDateTime;

public class AlertDTO {
    private String id;
    private String timestamp;
    private String description;
    private String code;
    private String username;
    private String employeeCode;

    public AlertDTO() {}

    public AlertDTO(Alert alert) {
        this.id = alert.getAlertId();
        this.timestamp = alert.getTimestamp() != null ? alert.getTimestamp().toString() : null;
        this.description = alert.getDescription();
        this.code = alert.getCode();
        this.username = alert.getUsername();
        this.employeeCode = alert.getEmployeeCode();
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmployeeCode() {
        return employeeCode;
    }

    public void setEmployeeCode(String employeeCode) {
        this.employeeCode = employeeCode;
    }
}



