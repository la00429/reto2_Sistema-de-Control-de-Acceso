package com.accesscontrol.accesscontrol.dto;

import java.time.LocalDateTime;

/**
 * DTO para reporte de empleados que accedieron en una fecha
 * Según especificación: documento, hora entrada, hora salida, duración
 */
public class EmployeeAccessReportDTO {
    private String document;  // Documento del empleado
    private String employeeCode;  // Código del empleado
    private String employeeName;  // Nombre completo
    private String entryTime;  // Hora de entrada
    private String exitTime;  // Hora de salida
    private String duration;  // Duración en horas y minutos (ej: "8h 30m")
    private LocalDateTime entryDateTime;
    private LocalDateTime exitDateTime;

    public EmployeeAccessReportDTO() {}

    public String getDocument() {
        return document;
    }

    public void setDocument(String document) {
        this.document = document;
    }

    public String getEmployeeCode() {
        return employeeCode;
    }

    public void setEmployeeCode(String employeeCode) {
        this.employeeCode = employeeCode;
    }

    public String getEmployeeName() {
        return employeeName;
    }

    public void setEmployeeName(String employeeName) {
        this.employeeName = employeeName;
    }

    public String getEntryTime() {
        return entryTime;
    }

    public void setEntryTime(String entryTime) {
        this.entryTime = entryTime;
    }

    public String getExitTime() {
        return exitTime;
    }

    public void setExitTime(String exitTime) {
        this.exitTime = exitTime;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public LocalDateTime getEntryDateTime() {
        return entryDateTime;
    }

    public void setEntryDateTime(LocalDateTime entryDateTime) {
        this.entryDateTime = entryDateTime;
    }

    public LocalDateTime getExitDateTime() {
        return exitDateTime;
    }

    public void setExitDateTime(LocalDateTime exitDateTime) {
        this.exitDateTime = exitDateTime;
    }
}





