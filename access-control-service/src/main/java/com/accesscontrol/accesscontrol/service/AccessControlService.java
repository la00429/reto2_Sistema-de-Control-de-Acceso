package com.accesscontrol.accesscontrol.service;

import com.accesscontrol.accesscontrol.dto.AccessRecordDTO;
import com.accesscontrol.accesscontrol.dto.AlertEvent;
import com.accesscontrol.accesscontrol.dto.EmployeeAccessReportDTO;
import com.accesscontrol.accesscontrol.dto.EmployeeDetailedReportDTO;
import com.accesscontrol.accesscontrol.exception.AccessValidationException;
import com.accesscontrol.accesscontrol.model.AccessRecord;
import com.accesscontrol.accesscontrol.repository.AccessRecordRepository;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class AccessControlService {
    
    @Autowired
    private AccessRecordRepository accessRecordRepository;
    
    @Autowired
    private RabbitTemplate rabbitTemplate;
    
    @Autowired
    private EmployeeClient employeeClient;
    
    private static final String ALERT_QUEUE = "alert.queue";
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public AccessRecordDTO registerAccess(AccessRecordDTO accessRecordDTO) {
        // Validar acceso antes de registrar
        validateAccess(accessRecordDTO);
        
        AccessRecord record = new AccessRecord();
        
        // Campos según especificación
        record.setEmployeeID(accessRecordDTO.getEmployeeID() != null ? 
                accessRecordDTO.getEmployeeID() : accessRecordDTO.getEmployeeCode());  // Usar documento o código
        record.setAccessTimestamp(accessRecordDTO.getAccessTimestamp() != null ? 
                accessRecordDTO.getAccessTimestamp() : LocalDateTime.now());
        // accessdatetime se genera automáticamente en @PrePersist
        
        // Campos adicionales para compatibilidad
        record.setEmployeeId(accessRecordDTO.getEmployeeId());
        record.setEmployeeCode(accessRecordDTO.getEmployeeCode());
        
        AccessRecord.AccessType accessType;
        try {
            accessType = AccessRecord.AccessType.valueOf(accessRecordDTO.getAccessType().toUpperCase());
            record.setAccessType(accessType);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid access type: " + accessRecordDTO.getAccessType());
        }
        record.setLocation(accessRecordDTO.getLocation());
        record.setDeviceId(accessRecordDTO.getDeviceId());
        
        try {
            record.setStatus(accessRecordDTO.getStatus() != null ? 
                    AccessRecord.AccessStatus.valueOf(accessRecordDTO.getStatus().toUpperCase()) : 
                    AccessRecord.AccessStatus.SUCCESS);
        } catch (IllegalArgumentException e) {
            record.setStatus(AccessRecord.AccessStatus.SUCCESS);
        }
        
        record.setNotes(accessRecordDTO.getNotes());
        
        AccessRecord saved = accessRecordRepository.save(record);
        return new AccessRecordDTO(saved);
    }
    
    private void validateAccess(AccessRecordDTO accessRecordDTO) {
        // Usar employeeID (documento) como identificador principal, o employeeCode como fallback
        String employeeIdentifier = accessRecordDTO.getEmployeeID() != null ? 
                accessRecordDTO.getEmployeeID() : accessRecordDTO.getEmployeeCode();
        AccessRecord.AccessType requestedType;
        
        try {
            requestedType = AccessRecord.AccessType.valueOf(accessRecordDTO.getAccessType().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid access type: " + accessRecordDTO.getAccessType());
        }
        
        // Obtener el último acceso del empleado (usar employeeID si está disponible)
        List<AccessRecord> latestRecords;
        if (accessRecordDTO.getEmployeeID() != null) {
            latestRecords = accessRecordRepository.findLatestByEmployeeID(accessRecordDTO.getEmployeeID());
        } else {
            latestRecords = accessRecordRepository.findLatestByEmployeeCode(accessRecordDTO.getEmployeeCode());
        }
        
        if (latestRecords.isEmpty()) {
            // Si no hay registros previos y se intenta salir, es un error
            if (requestedType == AccessRecord.AccessType.EXIT) {
                sendAlert("EMPLOYEE_ALREADY_LEFT", 
                    "Intento de salida sin ingreso previo para empleado: " + employeeIdentifier, 
                    employeeIdentifier);
                throw new AccessValidationException(
                    "No se puede registrar salida sin un ingreso previo", 
                    "EMPLOYEE_ALREADY_LEFT");
            }
            // Si es ingreso y no hay registros, está bien
            return;
        }
        
        AccessRecord lastRecord = latestRecords.get(0);
        
        if (requestedType == AccessRecord.AccessType.ENTRY) {
            // Validar que no haya un ingreso previo sin salida
            if (lastRecord.getAccessType() == AccessRecord.AccessType.ENTRY) {
                sendAlert("EMPLOYEE_ALREADY_ENTERED", 
                    "Intento de ingreso duplicado para empleado: " + employeeIdentifier + 
                    ". Último ingreso: " + lastRecord.getAccessdatetime(), 
                    employeeIdentifier);
                throw new AccessValidationException(
                    "El empleado ya tiene un ingreso registrado sin salida correspondiente", 
                    "EMPLOYEE_ALREADY_ENTERED");
            }
        } else if (requestedType == AccessRecord.AccessType.EXIT) {
            // Validar que haya un ingreso previo
            if (lastRecord.getAccessType() == AccessRecord.AccessType.EXIT) {
                sendAlert("EMPLOYEE_ALREADY_LEFT", 
                    "Intento de salida duplicado para empleado: " + employeeIdentifier + 
                    ". Última salida: " + lastRecord.getAccessdatetime(), 
                    employeeIdentifier);
                throw new AccessValidationException(
                    "El empleado ya tiene una salida registrada sin ingreso correspondiente", 
                    "EMPLOYEE_ALREADY_LEFT");
            }
        }
    }
    
    private void sendAlert(String code, String description, String employeeIdentifier) {
        try {
            AlertEvent alertEvent = new AlertEvent();
            alertEvent.setCode(code);
            alertEvent.setDescription(description);
            alertEvent.setEmployeeCode(employeeIdentifier);
            alertEvent.setTimestamp(LocalDateTime.now().toString());
            
            rabbitTemplate.convertAndSend(ALERT_QUEUE, alertEvent);
        } catch (Exception e) {
            // Log error pero no fallar la transacción
            System.err.println("Error enviando alerta: " + e.getMessage());
        }
    }

    public List<AccessRecordDTO> getAllAccessHistory() {
        return accessRecordRepository.findAll().stream()
                .map(AccessRecordDTO::new)
                .collect(Collectors.toList());
    }

    public List<AccessRecordDTO> getAccessHistoryByEmployee(Long employeeId) {
        return accessRecordRepository.findByEmployeeId(employeeId).stream()
                .map(AccessRecordDTO::new)
                .collect(Collectors.toList());
    }

    public List<AccessRecordDTO> getAccessHistoryByEmployeeCode(String employeeCode) {
        return accessRecordRepository.findByEmployeeCode(employeeCode).stream()
                .map(AccessRecordDTO::new)
                .collect(Collectors.toList());
    }

    public List<AccessRecordDTO> getAccessHistoryByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return accessRecordRepository.findByAccessTimestampBetween(startDate, endDate).stream()
                .map(AccessRecordDTO::new)
                .collect(Collectors.toList());
    }

    public List<AccessRecordDTO> getAccessHistoryByEmployeeAndDate(Long employeeId, LocalDateTime startDate, LocalDateTime endDate) {
        return accessRecordRepository.findByEmployeeIdAndDateRange(employeeId, startDate, endDate).stream()
                .map(AccessRecordDTO::new)
                .collect(Collectors.toList());
    }

    public List<AccessRecordDTO> getAccessHistoryByEmployeeCodeAndDate(String employeeCode, LocalDateTime startDate, LocalDateTime endDate) {
        return accessRecordRepository.findByEmployeeCodeAndDateRange(employeeCode, startDate, endDate).stream()
                .map(AccessRecordDTO::new)
                .collect(Collectors.toList());
    }

    public AccessRecordDTO getAccessRecordById(Long id) {
        AccessRecord record = accessRecordRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Access record not found with id: " + id));
        return new AccessRecordDTO(record);
    }
    
    /**
     * Reporte de empleados que accedieron en una fecha específica
     * Según especificación: documento, hora entrada, hora salida, duración
     */
    public List<EmployeeAccessReportDTO> getEmployeesAccessByDate(LocalDateTime date) {
        LocalDateTime startDate = date.withHour(0).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime endDate = date.withHour(23).withMinute(59).withSecond(59).withNano(999999999);
        
        List<AccessRecord> records = accessRecordRepository.findByAccessTimestampBetween(startDate, endDate);
        
        // Agrupar por empleado y calcular entradas/salidas
        Map<String, List<AccessRecord>> recordsByEmployee = records.stream()
                .collect(Collectors.groupingBy(ar -> 
                    ar.getEmployeeID() != null ? ar.getEmployeeID() : ar.getEmployeeCode()));
        
        List<EmployeeAccessReportDTO> report = new ArrayList<>();
        
        for (Map.Entry<String, List<AccessRecord>> entry : recordsByEmployee.entrySet()) {
            String employeeIdentifier = entry.getKey();
            List<AccessRecord> employeeRecords = entry.getValue();
            
            // Ordenar por timestamp
            employeeRecords.sort(Comparator.comparing(AccessRecord::getAccessTimestamp));
            
            // Buscar pares ENTRY-EXIT
            for (int i = 0; i < employeeRecords.size(); i++) {
                AccessRecord entryRecord = employeeRecords.get(i);
                if (entryRecord.getAccessType() == AccessRecord.AccessType.ENTRY) {
                    // Buscar la salida correspondiente
                    AccessRecord exitRecord = null;
                    for (int j = i + 1; j < employeeRecords.size(); j++) {
                        if (employeeRecords.get(j).getAccessType() == AccessRecord.AccessType.EXIT) {
                            exitRecord = employeeRecords.get(j);
                            break;
                        }
                    }
                    
                    EmployeeAccessReportDTO reportItem = new EmployeeAccessReportDTO();
                    reportItem.setDocument(entryRecord.getEmployeeID());
                    reportItem.setEmployeeCode(entryRecord.getEmployeeCode());
                    reportItem.setEntryDateTime(entryRecord.getAccessTimestamp());
                    reportItem.setEntryTime(entryRecord.getAccessTimestamp().format(TIME_FORMATTER));
                    
                    if (exitRecord != null) {
                        reportItem.setExitDateTime(exitRecord.getAccessTimestamp());
                        reportItem.setExitTime(exitRecord.getAccessTimestamp().format(TIME_FORMATTER));
                        
                        // Calcular duración
                        Duration duration = Duration.between(entryRecord.getAccessTimestamp(), exitRecord.getAccessTimestamp());
                        long hours = duration.toHours();
                        long minutes = duration.toMinutes() % 60;
                        reportItem.setDuration(String.format("%dh %dm", hours, minutes));
                    } else {
                        reportItem.setExitTime("Pendiente");
                        reportItem.setDuration("En curso");
                    }
                    
                    // Obtener información del empleado
                    Map<String, Object> employeeInfo = employeeClient.getEmployeeByDocument(entryRecord.getEmployeeID());
                    if (employeeInfo == null && entryRecord.getEmployeeCode() != null) {
                        employeeInfo = employeeClient.getEmployeeByCode(entryRecord.getEmployeeCode());
                    }
                    
                    if (employeeInfo != null) {
                        String firstName = (String) employeeInfo.get("firstname");
                        String lastName = (String) employeeInfo.get("lastname");
                        if (firstName != null && lastName != null) {
                            reportItem.setEmployeeName(firstName + " " + lastName);
                        }
                        if (reportItem.getDocument() == null) {
                            reportItem.setDocument((String) employeeInfo.get("document"));
                        }
                    }
                    
                    report.add(reportItem);
                }
            }
        }
        
        return report;
    }
    
    /**
     * Reporte de un empleado específico por rango de fechas
     * Según especificación: documento, fecha inicio, fecha fin
     */
    public List<EmployeeDetailedReportDTO> getEmployeeReportByDateRange(String document, LocalDateTime startDate, LocalDateTime endDate) {
        // Buscar empleado por documento
        Map<String, Object> employeeInfo = employeeClient.getEmployeeByDocument(document);
        if (employeeInfo == null) {
            throw new RuntimeException("Employee not found with document: " + document);
        }
        
        String employeeCode = employeeInfo.get("employeeCode") != null ? 
                (String) employeeInfo.get("employeeCode") : document;  // Usar documento como fallback
        
        // Obtener registros de acceso
        List<AccessRecord> records = accessRecordRepository.findByEmployeeCodeAndDateRange(employeeCode, startDate, endDate);
        
        // Si no hay registros por código, intentar por employeeID
        if (records.isEmpty()) {
            records = accessRecordRepository.findByEmployeeID(document);
            records = records.stream()
                    .filter(r -> r.getAccessTimestamp().isAfter(startDate) && r.getAccessTimestamp().isBefore(endDate))
                    .collect(Collectors.toList());
        }
        
        // Ordenar por fecha
        records.sort(Comparator.comparing(AccessRecord::getAccessTimestamp));
        
        String firstName = (String) employeeInfo.get("firstname");
        String lastName = (String) employeeInfo.get("lastname");
        String employeeName = (firstName != null && lastName != null) ? firstName + " " + lastName : document;
        
        return records.stream().map(record -> {
            EmployeeDetailedReportDTO dto = new EmployeeDetailedReportDTO();
            dto.setDocument(document);
            dto.setEmployeeCode(employeeCode);
            dto.setEmployeeName(employeeName);
            dto.setAccessDateTime(record.getAccessTimestamp());
            dto.setAccessType(record.getAccessType().name());
            dto.setAccessTime(record.getAccessTimestamp().format(TIME_FORMATTER));
            dto.setDate(record.getAccessTimestamp().format(DATE_FORMATTER));
            return dto;
        }).collect(Collectors.toList());
    }
}

