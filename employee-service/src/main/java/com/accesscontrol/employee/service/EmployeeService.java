package com.accesscontrol.employee.service;

import com.accesscontrol.employee.dto.EmployeeDTO;
import com.accesscontrol.employee.model.Employee;
import com.accesscontrol.employee.repository.EmployeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@Transactional
public class EmployeeService {
    
    @Autowired
    private EmployeeRepository employeeRepository;

    public List<EmployeeDTO> getAllEmployees() {
        return employeeRepository.findAll().stream()
                .map(EmployeeDTO::new)
                .collect(Collectors.toList());
    }

    public EmployeeDTO getEmployeeById(Long id) {
        Objects.requireNonNull(id, "Employee id is required");
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Employee not found with id: " + id));
        return new EmployeeDTO(employee);
    }

    public EmployeeDTO getEmployeeByCode(String employeeCode) {
        Employee employee = employeeRepository.findByEmployeeCode(employeeCode)
                .orElseThrow(() -> new RuntimeException("Employee not found with code: " + employeeCode));
        return new EmployeeDTO(employee);
    }

    public EmployeeDTO getEmployeeByDocument(String document) {
        Employee employee = employeeRepository.findByDocument(document)
                .orElseThrow(() -> new RuntimeException("Employee not found with document: " + document));
        return new EmployeeDTO(employee);
    }

    public EmployeeDTO createEmployee(EmployeeDTO employeeDTO) {
        if (employeeRepository.existsByDocument(employeeDTO.getDocument())) {
            throw new RuntimeException("Document already exists: " + employeeDTO.getDocument());
        }
        
        if (employeeRepository.existsByEmail(employeeDTO.getEmail())) {
            throw new RuntimeException("Email already exists: " + employeeDTO.getEmail());
        }
        
        Employee employee = new Employee();
        mapDTOToEntity(employeeDTO, employee);
        employee.setStatus(employeeDTO.getStatus() != null ? employeeDTO.getStatus() : true);  // true=activo por defecto
        
        Employee saved = employeeRepository.save(employee);
        return new EmployeeDTO(saved);
    }

    public EmployeeDTO updateEmployee(Long id, EmployeeDTO employeeDTO) {
        Objects.requireNonNull(id, "Employee id is required");
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Employee not found with id: " + id));
        
        // Verificar si el código de empleado ya existe en otro registro
        if (!employee.getEmployeeCode().equals(employeeDTO.getEmployeeCode()) &&
            employeeRepository.existsByEmployeeCode(employeeDTO.getEmployeeCode())) {
            throw new RuntimeException("Employee code already exists: " + employeeDTO.getEmployeeCode());
        }
        
        // Verificar si el email ya existe en otro registro
        if (!employee.getEmail().equals(employeeDTO.getEmail()) &&
            employeeRepository.existsByEmail(employeeDTO.getEmail())) {
            throw new RuntimeException("Email already exists: " + employeeDTO.getEmail());
        }
        
        mapDTOToEntity(employeeDTO, employee);
        Employee updated = employeeRepository.save(employee);
        return new EmployeeDTO(updated);
    }

    public void deleteEmployee(Long id) {
        throw new UnsupportedOperationException("Employee deletion is disabled. Use status updates instead.");
    }

    public EmployeeDTO updateEmployeeStatus(Long id, String status) {
        Objects.requireNonNull(id, "Employee id is required");
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Employee not found with id: " + id));
        
        // Convertir String a Boolean: "ACTIVE" o "true" = true, otros = false
        Boolean statusBoolean = "ACTIVE".equalsIgnoreCase(status) || "true".equalsIgnoreCase(status);
        employee.setStatus(statusBoolean);
        Employee updated = employeeRepository.save(employee);
        return new EmployeeDTO(updated);
    }

    private void mapDTOToEntity(EmployeeDTO dto, Employee entity) {
        entity.setDocument(dto.getDocument());
        entity.setEmployeeCode(dto.getEmployeeCode());
        entity.setFirstname(dto.getFirstname());
        entity.setLastname(dto.getLastname());
        entity.setEmail(dto.getEmail());
        entity.setPhone(dto.getPhone());
        entity.setDepartment(dto.getDepartment());
        entity.setPosition(dto.getPosition());
        
        if (dto.getStatus() != null) {
            entity.setStatus(dto.getStatus());  // Boolean directamente
        } else {
            entity.setStatus(true);  // true=activo por defecto
        }
    }
}

