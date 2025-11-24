package com.accesscontrol.accesscontrol.dto;

import java.time.LocalDateTime;

/**
 * DTO para reporte detallado de un empleado específico por rango de fechas
 */
public class EmployeeDetailedReportDTO {
    private String document;  // Documento del empleado
    private String employeeCode;
    private String employeeName;
    private LocalDateTime accessDateTime;
    private String accessType;  // ENTRY o EXIT
    private String accessTime;  // Hora formateada
    private String date;  // Fecha formateada

    public EmployeeDetailedReportDTO() {}

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

    public LocalDateTime getAccessDateTime() {
        return accessDateTime;
    }

    public void setAccessDateTime(LocalDateTime accessDateTime) {
        this.accessDateTime = accessDateTime;
    }

    public String getAccessType() {
        return accessType;
    }

    public void setAccessType(String accessType) {
        this.accessType = accessType;
    }

    public String getAccessTime() {
        return accessTime;
    }

    public void setAccessTime(String accessTime) {
        this.accessTime = accessTime;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}





