package com.accesscontrol.employee.controller;

import com.accesscontrol.employee.dto.EmployeeDTO;
import com.accesscontrol.employee.service.EmployeeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/employee")
@Tag(name = "Employee", description = "API para gestión de empleados")
public class EmployeeController {
    
    private static final Logger logger = LoggerFactory.getLogger(EmployeeController.class);
    private final EmployeeService employeeService;

    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    @Operation(summary = "Obtener todos los empleados", description = "Retorna la lista completa de empleados activos e inactivos")
    @ApiResponse(responseCode = "200", description = "Lista de empleados obtenida exitosamente")
    @GetMapping("/findallemployees")
    public ResponseEntity<List<EmployeeDTO>> getAllEmployees() {
        List<EmployeeDTO> employees = employeeService.getAllEmployees();
        return ResponseEntity.ok(employees);
    }

    @GetMapping("/{id}")
    public ResponseEntity<EmployeeDTO> getEmployeeById(@PathVariable Long id) {
        EmployeeDTO employee = employeeService.getEmployeeById(id);
        return ResponseEntity.ok(employee);
    }

    @GetMapping("/code/{employeeCode}")
    public ResponseEntity<EmployeeDTO> getEmployeeByCode(@PathVariable String employeeCode) {
        EmployeeDTO employee = employeeService.getEmployeeByCode(employeeCode);
        return ResponseEntity.ok(employee);
    }

    @GetMapping("/document/{document}")
    public ResponseEntity<EmployeeDTO> getEmployeeByDocument(@PathVariable String document) {
        EmployeeDTO employee = employeeService.getEmployeeByDocument(document);
        return ResponseEntity.ok(employee);
    }

    @Operation(summary = "Crear nuevo empleado", description = "Registra un nuevo empleado en el sistema")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Empleado creado exitosamente"),
        @ApiResponse(responseCode = "400", description = "Datos inválidos o empleado ya existe")
    })
    @PostMapping("/createemployee")
    public ResponseEntity<?> createEmployee(@Valid @RequestBody EmployeeDTO employeeDTO) {
        try {
            logger.info("Creando empleado - Document: {}, Email: {}, Firstname: {}, Lastname: {}", 
                employeeDTO.getDocument(), employeeDTO.getEmail(), 
                employeeDTO.getFirstname(), employeeDTO.getLastname());
            
            EmployeeDTO created = employeeService.createEmployee(employeeDTO);
            logger.info("Empleado creado exitosamente con ID: {}", created.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (RuntimeException e) {
            logger.error("Error al crear empleado: {}", e.getMessage(), e);
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        } catch (Exception e) {
            logger.error("Error inesperado al crear empleado", e);
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Error interno del servidor");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
            logger.warn("Error de validación en campo {}: {}", fieldName, errorMessage);
        });
        
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("error", "Validation failed");
        errorResponse.put("message", "Los datos proporcionados no son válidos");
        errorResponse.put("errors", errors);
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @Operation(summary = "Actualizar empleado", description = "Actualiza la información de un empleado existente")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Empleado actualizado exitosamente"),
        @ApiResponse(responseCode = "404", description = "Empleado no encontrado")
    })
    @PutMapping("/updateemployee/{id}")
    public ResponseEntity<EmployeeDTO> updateEmployee(@PathVariable Long id, @Valid @RequestBody EmployeeDTO employeeDTO) {
        EmployeeDTO updated = employeeService.updateEmployee(id, employeeDTO);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEmployee(@PathVariable Long id) {
        throw new ResponseStatusException(
                HttpStatus.METHOD_NOT_ALLOWED,
                "La eliminación física de empleados está deshabilitada. Usa los endpoints de activación/inactivación.");
    }

    @Operation(summary = "Inactivar empleado", description = "Desactiva un empleado sin eliminarlo del sistema")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Empleado inactivado exitosamente"),
        @ApiResponse(responseCode = "404", description = "Empleado no encontrado")
    })
    @PatchMapping("/disableemployee/{id}")
    public ResponseEntity<EmployeeDTO> disableEmployee(@PathVariable Long id) {
        EmployeeDTO updated = employeeService.updateEmployeeStatus(id, "INACTIVE");
        return ResponseEntity.ok(updated);
    }

    @Operation(summary = "Activar empleado", description = "Activa nuevamente a un empleado previamente inactivo")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Empleado activado exitosamente"),
        @ApiResponse(responseCode = "404", description = "Empleado no encontrado")
    })
    @PatchMapping("/enableemployee/{id}")
    public ResponseEntity<EmployeeDTO> enableEmployee(@PathVariable Long id) {
        EmployeeDTO updated = employeeService.updateEmployeeStatus(id, "ACTIVE");
        return ResponseEntity.ok(updated);
    }
}

