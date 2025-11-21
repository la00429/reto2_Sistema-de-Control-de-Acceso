package com.accesscontrol.accesscontrol.dto;

import com.accesscontrol.accesscontrol.model.AccessRecord;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public class AccessRecordDTO {
    private Long id;
    
    @NotBlank(message = "Employee ID (document) is required")
    private String employeeID;  // String - documento según especificación
    
    private String accessdatetime;  // String según especificación
    
    // Campos adicionales para compatibilidad
    private Long employeeId;
    
    private String employeeCode;
    
    @NotBlank(message = "Access type is required")
    private String accessType;
    
    private LocalDateTime accessTimestamp;
    
    private String location;
    
    private String deviceId;
    
    private String status;
    
    private String notes;

    public AccessRecordDTO() {}

    public AccessRecordDTO(AccessRecord record) {
        this.id = record.getId();
        this.employeeID = record.getEmployeeID();  // Campo según especificación
        this.accessdatetime = record.getAccessdatetime();  // Campo según especificación
        this.employeeId = record.getEmployeeId();
        this.employeeCode = record.getEmployeeCode();
        this.accessType = record.getAccessType() != null ? record.getAccessType().name() : null;
        this.accessTimestamp = record.getAccessTimestamp();
        this.location = record.getLocation();
        this.deviceId = record.getDeviceId();
        this.status = record.getStatus() != null ? record.getStatus().name() : null;
        this.notes = record.getNotes();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(Long employeeId) {
        this.employeeId = employeeId;
    }

    public String getEmployeeCode() {
        return employeeCode;
    }

    public void setEmployeeCode(String employeeCode) {
        this.employeeCode = employeeCode;
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

