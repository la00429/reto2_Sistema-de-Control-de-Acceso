package com.accesscontrol.accesscontrol.infrastructure.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;

import java.time.LocalDateTime;

/**
 * DTO para capa web
 * Arquitectura Hexagonal: Infrastructure Layer
 */
public class AccessRecordDTO {
    private Long id;
    
    @NotBlank(message = "Employee ID (document) is required")
    private String employeeID;
    
    private String accessdatetime;
    private String accessType;
    private LocalDateTime accessTimestamp;
    private String location;
    private String deviceId;
    private String status;
    private String notes;

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmployeeID() {
        return employeeID;
    }

    public void setEmployeeID(String employeeID) {
        this.employeeID = employeeID;
    }

    public String getAccessdatetime() {
        return accessdatetime;
    }

    public void setAccessdatetime(String accessdatetime) {
        this.accessdatetime = accessdatetime;
    }

    public String getAccessType() {
        return accessType;
    }

    public void setAccessType(String accessType) {
        this.accessType = accessType;
    }

    public LocalDateTime getAccessTimestamp() {
        return accessTimestamp;
    }

    public void setAccessTimestamp(LocalDateTime accessTimestamp) {
        this.accessTimestamp = accessTimestamp;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}





