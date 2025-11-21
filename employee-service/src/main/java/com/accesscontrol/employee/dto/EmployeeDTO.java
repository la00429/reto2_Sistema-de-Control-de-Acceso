package com.accesscontrol.employee.dto;

import com.accesscontrol.employee.model.Employee;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class EmployeeDTO {
    private Long id;
    
    @NotBlank(message = "Document is required")
    @Size(max = 50, message = "Document must not exceed 50 characters")
    private String document;  // Identificador principal según especificación
    
    @Size(max = 50, message = "Employee code must not exceed 50 characters")
    private String employeeCode;  // Mantener para compatibilidad
    
    @NotBlank(message = "First name is required")
    @Size(max = 100, message = "First name must not exceed 100 characters")
    private String firstname;  // Según especificación
    
    @NotBlank(message = "Last name is required")
    @Size(max = 100, message = "Last name must not exceed 100 characters")
    private String lastname;  // Según especificación
    
    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    @Size(max = 255, message = "Email must not exceed 255 characters")
    private String email;
    
    @Size(max = 20, message = "Phone must not exceed 20 characters")
    private String phone;
    
    @Size(max = 100, message = "Department must not exceed 100 characters")
    private String department;
    
    @Size(max = 100, message = "Position must not exceed 100 characters")
    private String position;
    
    private Boolean status;  // Boolean según especificación: true=activo, false=inactivo

    // Constructors
    public EmployeeDTO() {}

    public EmployeeDTO(Employee employee) {
        this.id = employee.getId();
        this.document = employee.getDocument();
        this.employeeCode = employee.getEmployeeCode();
        this.firstname = employee.getFirstname();
        this.lastname = employee.getLastname();
        this.email = employee.getEmail();
        this.phone = employee.getPhone();
        this.department = employee.getDepartment();
        this.position = employee.getPosition();
        this.status = employee.getStatus();  // Boolean directamente
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmployeeCode() {
        return employeeCode;
    }

    public void setEmployeeCode(String employeeCode) {
        this.employeeCode = employeeCode;
    }

    public String getDocument() {
        return document;
    }

    public void setDocument(String document) {
        this.document = document;
    }

    public String getFirstname() {
        return firstname;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public Boolean getStatus() {
        return status;
    }

    public void setStatus(Boolean status) {
        this.status = status;
    }
}

