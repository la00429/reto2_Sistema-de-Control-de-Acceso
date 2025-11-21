package com.accesscontrol.accesscontrol.infrastructure.adapter.in.web;

import com.accesscontrol.accesscontrol.application.usecase.RegisterAccessUseCase;
import com.accesscontrol.accesscontrol.domain.model.AccessRecord;
import com.accesscontrol.accesscontrol.dto.AccessRecordDTO;
import com.accesscontrol.accesscontrol.dto.EmployeeAccessReportDTO;
import com.accesscontrol.accesscontrol.dto.EmployeeDetailedReportDTO;
import com.accesscontrol.accesscontrol.exception.AccessValidationException;
import com.accesscontrol.accesscontrol.infrastructure.adapter.in.web.mapper.AccessRecordWebMapper;
import com.accesscontrol.accesscontrol.service.AccessControlService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controlador Web
 * Arquitectura Hexagonal: Infrastructure Layer - Adaptador de entrada
 */
@RestController
@RequestMapping("/access")
@CrossOrigin(origins = "*")
@Tag(name = "Access Control", description = "API para control de acceso de empleados")
public class AccessControlController {
    
    private final RegisterAccessUseCase registerAccessUseCase;
    private final AccessRecordWebMapper mapper;
    private final AccessControlService accessControlService;

    public AccessControlController(RegisterAccessUseCase registerAccessUseCase, 
                                   AccessRecordWebMapper mapper,
                                   AccessControlService accessControlService) {
        this.registerAccessUseCase = registerAccessUseCase;
        this.mapper = mapper;
        this.accessControlService = accessControlService;
    }

    @Operation(summary = "Registrar ingreso", description = "Registra el ingreso de un empleado al sistema")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Ingreso registrado exitosamente"),
        @ApiResponse(responseCode = "400", description = "Error de validación o doble ingreso")
    })
    @PostMapping("/usercheckin")
    public ResponseEntity<?> userCheckIn(@Valid @RequestBody AccessRecordDTO accessRecordDTO) {
        try {
            AccessRecord domainRecord = mapper.toDomainEntity(accessRecordDTO);
            domainRecord.setAccessType(AccessRecord.AccessType.ENTRY);

            AccessRecord saved = registerAccessUseCase.execute(domainRecord);
            return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toDTO(saved));
        } catch (AccessValidationException ex) {
            return buildValidationError(ex);
        } catch (RuntimeException ex) {
            return buildUnexpectedError(ex);
        }
    }

    @Operation(summary = "Registrar salida", description = "Registra la salida de un empleado del sistema")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Salida registrada exitosamente"),
        @ApiResponse(responseCode = "400", description = "Error de validación o doble salida")
    })
    @PostMapping("/usercheckout")
    public ResponseEntity<?> userCheckOut(@Valid @RequestBody AccessRecordDTO accessRecordDTO) {
        try {
            AccessRecord domainRecord = mapper.toDomainEntity(accessRecordDTO);
            domainRecord.setAccessType(AccessRecord.AccessType.EXIT);

            AccessRecord saved = registerAccessUseCase.execute(domainRecord);
            return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toDTO(saved));
        } catch (AccessValidationException ex) {
            return buildValidationError(ex);
        } catch (RuntimeException ex) {
            return buildUnexpectedError(ex);
        }
    }

    @Operation(summary = "Reporte de empleados por fecha", description = "Obtiene el reporte de todos los empleados que accedieron en una fecha específica")
    @ApiResponse(responseCode = "200", description = "Reporte generado exitosamente")
    @GetMapping("/allemployeesbydate")
    public ResponseEntity<List<EmployeeAccessReportDTO>> getAllEmployeesByDate(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime date) {
        List<EmployeeAccessReportDTO> report = accessControlService.getEmployeesAccessByDate(date);
        return ResponseEntity.ok(report);
    }

    @Operation(summary = "Reporte de empleado por rango de fechas", description = "Obtiene el reporte detallado de accesos de un empleado en un rango de fechas")
    @ApiResponse(responseCode = "200", description = "Reporte generado exitosamente")
    @GetMapping("/employeebydates")
    public ResponseEntity<List<EmployeeDetailedReportDTO>> getEmployeeByDates(
            @RequestParam String document,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        List<EmployeeDetailedReportDTO> report = accessControlService.getEmployeeReportByDateRange(document, startDate, endDate);
        return ResponseEntity.ok(report);
    }

    // Endpoints adicionales para compatibilidad
    @PostMapping("/register")
    public ResponseEntity<?> registerAccess(@Valid @RequestBody AccessRecordDTO accessRecordDTO) {
        try {
            AccessRecord domainRecord = mapper.toDomainEntity(accessRecordDTO);
            AccessRecord saved = registerAccessUseCase.execute(domainRecord);
            return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toDTO(saved));
        } catch (AccessValidationException ex) {
            return buildValidationError(ex);
        } catch (RuntimeException ex) {
            return buildUnexpectedError(ex);
        }
    }

    @GetMapping("/history")
    public ResponseEntity<List<AccessRecordDTO>> getAccessHistory(
            @RequestParam(required = false) Long employeeId,
            @RequestParam(required = false) String employeeCode,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        
        List<AccessRecordDTO> records;
        
        if (employeeId != null && startDate != null && endDate != null) {
            records = accessControlService.getAccessHistoryByEmployeeAndDate(employeeId, startDate, endDate);
        } else if (employeeCode != null && startDate != null && endDate != null) {
            records = accessControlService.getAccessHistoryByEmployeeCodeAndDate(employeeCode, startDate, endDate);
        } else if (employeeId != null) {
            records = accessControlService.getAccessHistoryByEmployee(employeeId);
        } else if (employeeCode != null) {
            records = accessControlService.getAccessHistoryByEmployeeCode(employeeCode);
        } else if (startDate != null && endDate != null) {
            records = accessControlService.getAccessHistoryByDateRange(startDate, endDate);
        } else {
            records = accessControlService.getAllAccessHistory();
        }
        
        return ResponseEntity.ok(records);
    }

    @GetMapping("/{id}")
    public ResponseEntity<AccessRecordDTO> getAccessRecordById(@PathVariable Long id) {
        AccessRecordDTO record = accessControlService.getAccessRecordById(id);
        return ResponseEntity.ok(record);
    }

    private ResponseEntity<Map<String, Object>> buildValidationError(AccessValidationException ex) {
        Map<String, Object> error = new HashMap<>();
        error.put("success", false);
        error.put("message", ex.getMessage());
        error.put("alertCode", ex.getAlertCode());
        error.put("status", HttpStatus.BAD_REQUEST.value());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    private ResponseEntity<Map<String, Object>> buildUnexpectedError(RuntimeException ex) {
        Map<String, Object> error = new HashMap<>();
        error.put("success", false);
        error.put("message", ex.getMessage());
        error.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}

