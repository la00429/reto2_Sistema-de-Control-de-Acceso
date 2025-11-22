package com.accesscontrol.accesscontrol.domain.model;

import java.time.LocalDateTime;

/**
 * Entidad de dominio - AccessRecord
 * Arquitectura Hexagonal: Domain Layer
 */
public class AccessRecord {
    private Long id;
    private String employeeID;
    private String employeeCode;  // Código del empleado
    private String accessdatetime;
    private AccessType accessType;
    private LocalDateTime accessTimestamp;
    private String location;
    private String deviceId;
    private AccessStatus status;
    private String notes;

    public AccessRecord() {}

    public AccessRecord(String employeeID, AccessType accessType) {
        this.employeeID = employeeID;
        this.accessType = accessType;
        this.accessTimestamp = LocalDateTime.now();
        this.status = AccessStatus.SUCCESS;
        if (this.accessdatetime == null) {
            this.accessdatetime = this.accessTimestamp.format(
                java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        }
    }

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

    public String getEmployeeCode() {
        return employeeCode;
    }

    public void setEmployeeCode(String employeeCode) {
        this.employeeCode = employeeCode;
    }

    public String getAccessdatetime() {
        return accessdatetime;
    }

    public void setAccessdatetime(String accessdatetime) {
        this.accessdatetime = accessdatetime;
    }

    public AccessType getAccessType() {
        return accessType;
    }

    public void setAccessType(AccessType accessType) {
        this.accessType = accessType;
    }

    public LocalDateTime getAccessTimestamp() {
        return accessTimestamp;
    }

    public void setAccessTimestamp(LocalDateTime accessTimestamp) {
        this.accessTimestamp = accessTimestamp;
        if (this.accessdatetime == null && accessTimestamp != null) {
            this.accessdatetime = accessTimestamp.format(
                java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        }
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

    public AccessStatus getStatus() {
        return status;
    }

    public void setStatus(AccessStatus status) {
        this.status = status;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    // Lógica de dominio
    public boolean isEntry() {
        return accessType == AccessType.ENTRY;
    }

    public boolean isExit() {
        return accessType == AccessType.EXIT;
    }

    public enum AccessType {
        ENTRY, EXIT
    }

    public enum AccessStatus {
        SUCCESS, FAILED, BLOCKED
    }
}



