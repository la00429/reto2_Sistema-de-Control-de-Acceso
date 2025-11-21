package com.accesscontrol.accesscontrol.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "access")
public class AccessRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "employeeID", nullable = false, length = 50)
    private String employeeID;  // String - documento del empleado según especificación
    
    @Column(name = "accessdatetime", nullable = false, length = 50)
    private String accessdatetime;  // String según especificación
    
    // Campos adicionales para funcionalidad extendida
    @Column(name = "employee_id")
    private Long employeeId;  // Mantener para compatibilidad
    
    @Column(name = "employee_code", length = 50)
    private String employeeCode;  // Mantener para compatibilidad
    
    @Enumerated(EnumType.STRING)
    @Column(name = "access_type")
    private AccessType accessType;  // Para lógica interna
    
    @Column(name = "access_timestamp")
    private LocalDateTime accessTimestamp;  // Para consultas y lógica interna
    
    @Column(length = 100)
    private String location;
    
    @Column(name = "device_id", length = 100)
    private String deviceId;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AccessStatus status;
    
    @Column(columnDefinition = "TEXT")
    private String notes;

    @PrePersist
    protected void onCreate() {
        if (accessTimestamp == null) {
            accessTimestamp = LocalDateTime.now();
        }
        if (accessdatetime == null || accessdatetime.isEmpty()) {
            // Formato: yyyy-MM-dd HH:mm:ss
            accessdatetime = accessTimestamp.format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
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

    public String getAccessdatetime() {
        return accessdatetime;
    }

    public void setAccessdatetime(String accessdatetime) {
        this.accessdatetime = accessdatetime;
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

    public enum AccessType {
        ENTRY, EXIT
    }

    public enum AccessStatus {
        SUCCESS, FAILED, BLOCKED
    }
}

