package com.accesscontrol.accesscontrol.application.service;

import com.accesscontrol.accesscontrol.domain.model.AccessRecord;
import com.accesscontrol.accesscontrol.domain.port.out.AccessRecordRepositoryPort;
import com.accesscontrol.accesscontrol.domain.port.out.EmployeeServicePort;
import com.accesscontrol.accesscontrol.dto.AccessRecordDTO;
import com.accesscontrol.accesscontrol.dto.EmployeeAccessReportDTO;
import com.accesscontrol.accesscontrol.dto.EmployeeDetailedReportDTO;
import com.accesscontrol.accesscontrol.exception.AccessValidationException;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.accesscontrol.accesscontrol.config.MetricsConfig.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Servicio de Aplicación para Control de Acceso
 * Arquitectura Hexagonal: Application Layer
 * 
 * Este servicio orquesta las operaciones de negocio relacionadas con el control de acceso,
 * coordinando entre los puertos de salida (repositorio, servicio de empleados, alertas)
 * y aplicando las reglas de negocio.
 */
@Service
@Transactional
public class AccessControlService {
    
    @Autowired
    private AccessRecordRepositoryPort accessRecordRepositoryPort;
    
    @Autowired
    private RabbitTemplate rabbitTemplate;
    
    @Autowired
    private EmployeeServicePort employeeServicePort;
    
    // Métricas Prometheus
    @Autowired
    @Qualifier("accessRecordsCreatedCounter")
    private Counter accessRecordsCreatedCounter;
    
    @Autowired
    private MeterRegistry meterRegistry;
    
    @Autowired
    @Qualifier("accessRegistrationTimer")
    private Timer accessRegistrationTimer;
    
    @Autowired
    @Qualifier("accessValidationTimer")
    private Timer accessValidationTimer;
    
    @Autowired
    @Qualifier("accessHistoryQueryTimer")
    private Timer accessHistoryQueryTimer;
    
    @Autowired
    @Qualifier("employeeServiceCallTimer")
    private Timer employeeServiceCallTimer;
    
    private static final String ALERT_QUEUE = "alert.queue";
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    /**
     * Registra un nuevo acceso de empleado
     */
    public AccessRecordDTO registerAccess(AccessRecordDTO accessRecordDTO) {
        try {
            return accessRegistrationTimer.recordCallable(() -> {
                // Validar acceso antes de registrar
                validateAccess(accessRecordDTO);
                
                // Obtener el employeeCode: primero del DTO, si no viene o está vacío, obtenerlo del servicio de empleados
                String employeeCode = accessRecordDTO.getEmployeeCode();
                if ((employeeCode == null || employeeCode.trim().isEmpty()) && 
                    accessRecordDTO.getEmployeeID() != null && !accessRecordDTO.getEmployeeID().trim().isEmpty()) {
                    try {
                        Map<String, Object> employeeInfo = employeeServiceCallTimer.recordCallable(() -> 
                            employeeServicePort.getEmployeeByDocument(accessRecordDTO.getEmployeeID()));
                        if (employeeInfo != null && employeeInfo.containsKey("employeeCode")) {
                            employeeCode = (String) employeeInfo.get("employeeCode");
                            // Solo actualizar si se obtuvo un valor válido
                            if (employeeCode != null && !employeeCode.trim().isEmpty()) {
                                accessRecordDTO.setEmployeeCode(employeeCode);
                                System.out.println("Código del empleado obtenido del servicio: " + employeeCode + " para documento: " + accessRecordDTO.getEmployeeID());
                            }
                        }
                    } catch (Exception e) {
                        // Si falla la consulta, continuar sin employeeCode
                        System.err.println("No se pudo obtener el código del empleado para documento " + accessRecordDTO.getEmployeeID() + ": " + e.getMessage());
                    }
                }
                
                // Convertir DTO a entidad de dominio
                AccessRecord record = convertToDomainEntity(accessRecordDTO, employeeCode);
                
                // Guardar en el repositorio
                AccessRecord saved = accessRecordRepositoryPort.save(record);
                
                // Incrementar métricas
                accessRecordsCreatedCounter.increment();
                if (record.getAccessType() != null) {
                    getAccessRecordsByTypeCounter(meterRegistry, record.getAccessType().name().toLowerCase()).increment();
                }
                
                return convertToDTO(saved);
            });
        } catch (Exception e) {
            throw new RuntimeException("Error registering access: " + e.getMessage(), e);
        }
    }
    
    /**
     * Convierte un DTO a entidad de dominio
     */
    private AccessRecord convertToDomainEntity(AccessRecordDTO dto, String employeeCode) {
        AccessRecord record = new AccessRecord();
        
        // Campos según especificación
        record.setEmployeeID(dto.getEmployeeID() != null ? 
                dto.getEmployeeID() : dto.getEmployeeCode());
        record.setAccessTimestamp(dto.getAccessTimestamp() != null ? 
                dto.getAccessTimestamp() : LocalDateTime.now());
        
        // Usar el employeeCode obtenido (puede ser del DTO o del servicio de empleados)
        if (employeeCode != null && !employeeCode.trim().isEmpty()) {
            record.setEmployeeCode(employeeCode);
        } else if (dto.getEmployeeCode() != null && !dto.getEmployeeCode().trim().isEmpty()) {
            record.setEmployeeCode(dto.getEmployeeCode());
        }
        
        AccessRecord.AccessType accessType;
        try {
            accessType = AccessRecord.AccessType.valueOf(dto.getAccessType().toUpperCase());
            record.setAccessType(accessType);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid access type: " + dto.getAccessType());
        }
        
        record.setLocation(dto.getLocation());
        record.setDeviceId(dto.getDeviceId());
        
        try {
            record.setStatus(dto.getStatus() != null ? 
                    AccessRecord.AccessStatus.valueOf(dto.getStatus().toUpperCase()) : 
                    AccessRecord.AccessStatus.SUCCESS);
        } catch (IllegalArgumentException e) {
            record.setStatus(AccessRecord.AccessStatus.SUCCESS);
        }
        
        record.setNotes(dto.getNotes());
        
        return record;
    }
    
    /**
     * Convierte una entidad de dominio a DTO
     */
    private AccessRecordDTO convertToDTO(AccessRecord record) {
        AccessRecordDTO dto = new AccessRecordDTO();
        dto.setId(record.getId());
        dto.setEmployeeID(record.getEmployeeID());
        dto.setEmployeeCode(record.getEmployeeCode());
        dto.setAccessdatetime(record.getAccessdatetime());
        dto.setAccessType(record.getAccessType() != null ? record.getAccessType().name() : null);
        dto.setAccessTimestamp(record.getAccessTimestamp());
        dto.setLocation(record.getLocation());
        dto.setDeviceId(record.getDeviceId());
        dto.setStatus(record.getStatus() != null ? record.getStatus().name() : null);
        dto.setNotes(record.getNotes());
        return dto;
    }
    
    /**
     * Valida un acceso antes de registrarlo
     */
    private void validateAccess(AccessRecordDTO accessRecordDTO) {
        accessValidationTimer.record(() -> {
            try {
                // Usar employeeID (documento) como identificador principal, o employeeCode como fallback
                String employeeIdentifier = accessRecordDTO.getEmployeeID() != null ? 
                        accessRecordDTO.getEmployeeID() : accessRecordDTO.getEmployeeCode();
                AccessRecord.AccessType requestedType;
                
                try {
                    requestedType = AccessRecord.AccessType.valueOf(accessRecordDTO.getAccessType().toUpperCase());
                } catch (IllegalArgumentException e) {
                    throw new RuntimeException("Invalid access type: " + accessRecordDTO.getAccessType());
                }
                
                // Obtener el último acceso del empleado
                List<AccessRecord> latestRecords;
                if (accessRecordDTO.getEmployeeID() != null) {
                    latestRecords = accessRecordRepositoryPort.findLatestByEmployeeID(accessRecordDTO.getEmployeeID());
                } else {
                    latestRecords = accessRecordRepositoryPort.findLatestByEmployeeCode(accessRecordDTO.getEmployeeCode());
                }
                
                if (latestRecords.isEmpty()) {
                    // Si no hay registros previos y se intenta salir, es un error
                    if (requestedType == AccessRecord.AccessType.EXIT) {
                        getAccessValidationFailedCounter(meterRegistry, "exit_without_entry").increment();
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
                        getAccessValidationFailedCounter(meterRegistry, "duplicate_entry").increment();
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
                        getAccessValidationFailedCounter(meterRegistry, "duplicate_exit").increment();
                        sendAlert("EMPLOYEE_ALREADY_LEFT", 
                            "Intento de salida duplicado para empleado: " + employeeIdentifier + 
                            ". Última salida: " + lastRecord.getAccessdatetime(), 
                            employeeIdentifier);
                        throw new AccessValidationException(
                            "El empleado ya tiene una salida registrada sin ingreso correspondiente", 
                            "EMPLOYEE_ALREADY_LEFT");
                    }
                }
            } catch (AccessValidationException e) {
                throw e;
            } catch (Exception e) {
                getAccessValidationFailedCounter(meterRegistry, "unknown").increment();
                throw new RuntimeException("Error validating access: " + e.getMessage(), e);
            }
        });
    }
    
    /**
     * Envía una alerta a través de RabbitMQ
     */
    private void sendAlert(String code, String description, String employeeIdentifier) {
        try {
            com.accesscontrol.accesscontrol.dto.AlertEvent alertEvent = new com.accesscontrol.accesscontrol.dto.AlertEvent();
            alertEvent.setCode(code);
            alertEvent.setDescription(description);
            alertEvent.setEmployeeCode(employeeIdentifier);
            alertEvent.setTimestamp(LocalDateTime.now().toString());
            
            rabbitTemplate.convertAndSend(ALERT_QUEUE, alertEvent);
            getAlertsSentCounter(meterRegistry, code).increment();
        } catch (Exception e) {
            // Log error pero no fallar la transacción
            System.err.println("Error enviando alerta: " + e.getMessage());
        }
    }

    /**
     * Obtiene todo el historial de acceso con actualización de employeeCode si falta
     */
    public List<AccessRecordDTO> getAllAccessHistory() {
        try {
            return accessHistoryQueryTimer.recordCallable(() -> {
                List<AccessRecord> records = accessRecordRepositoryPort.findAll();
                System.out.println("getAllAccessHistory: Encontrados " + records.size() + " registros");
                
                // Actualizar registros que no tengan employeeCode
                for (AccessRecord record : records) {
                    if ((record.getEmployeeCode() == null || record.getEmployeeCode().trim().isEmpty()) 
                        && record.getEmployeeID() != null && !record.getEmployeeID().trim().isEmpty()) {
                        try {
                            System.out.println("Buscando employeeCode para registro ID " + record.getId() + " con documento " + record.getEmployeeID());
                            Map<String, Object> employeeInfo = employeeServicePort.getEmployeeByDocument(record.getEmployeeID());
                            if (employeeInfo != null && employeeInfo.containsKey("employeeCode")) {
                                String employeeCode = (String) employeeInfo.get("employeeCode");
                                System.out.println("Encontrado employeeCode: " + employeeCode + " para documento " + record.getEmployeeID());
                                if (employeeCode != null && !employeeCode.trim().isEmpty()) {
                                    record.setEmployeeCode(employeeCode);
                                    accessRecordRepositoryPort.save(record);
                                    System.out.println("✓ Actualizado employeeCode para registro " + record.getId() + ": " + record.getEmployeeCode());
                                }
                            } else {
                                System.out.println("✗ No se encontró employeeCode en la respuesta del servicio para documento " + record.getEmployeeID());
                            }
                        } catch (Exception e) {
                            System.err.println("✗ ERROR: No se pudo obtener el código del empleado para documento " + record.getEmployeeID() + ": " + e.getMessage());
                            e.printStackTrace();
                        }
                    } else {
                        System.out.println("Registro ID " + record.getId() + " ya tiene employeeCode: " + record.getEmployeeCode());
                    }
                }
                
                // Recargar los registros después de las actualizaciones
                records = accessRecordRepositoryPort.findAll();
                
                // Convertir a DTOs
                return records.stream()
                        .map(this::convertToDTO)
                        .collect(Collectors.toList());
            });
        } catch (Exception e) {
            throw new RuntimeException("Error getting access history: " + e.getMessage(), e);
        }
    }

    public List<AccessRecordDTO> getAccessHistoryByEmployee(Long employeeId) {
        return accessRecordRepositoryPort.findByEmployeeId(employeeId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<AccessRecordDTO> getAccessHistoryByEmployeeCode(String employeeCode) {
        return accessRecordRepositoryPort.findByEmployeeCode(employeeCode).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<AccessRecordDTO> getAccessHistoryByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return accessRecordRepositoryPort.findByAccessTimestampBetween(startDate, endDate).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<AccessRecordDTO> getAccessHistoryByEmployeeAndDate(Long employeeId, LocalDateTime startDate, LocalDateTime endDate) {
        return accessRecordRepositoryPort.findByEmployeeIdAndDateRange(employeeId, startDate, endDate).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<AccessRecordDTO> getAccessHistoryByEmployeeCodeAndDate(String employeeCode, LocalDateTime startDate, LocalDateTime endDate) {
        return accessRecordRepositoryPort.findByEmployeeCodeAndDateRange(employeeCode, startDate, endDate).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public AccessRecordDTO getAccessRecordById(Long id) {
        AccessRecord record = accessRecordRepositoryPort.findById(id)
                .orElseThrow(() -> new RuntimeException("Access record not found with id: " + id));
        return convertToDTO(record);
    }

    public List<EmployeeAccessReportDTO> getEmployeesAccessByDate(LocalDateTime date) {
        LocalDateTime startOfDay = date.toLocalDate().atStartOfDay();
        LocalDateTime endOfDay = date.toLocalDate().atTime(23, 59, 59);

        List<AccessRecord> records = accessRecordRepositoryPort.findByAccessTimestampBetween(startOfDay, endOfDay);

        Map<String, EmployeeAccessReportDTO> employeeAccessMap = new HashMap<>();

        for (AccessRecord record : records) {
            String employeeDocument = record.getEmployeeID();
            final String finalEmployeeCode = getOrFetchEmployeeCode(record, employeeDocument);

            employeeAccessMap.computeIfAbsent(employeeDocument, doc -> {
                EmployeeAccessReportDTO dto = new EmployeeAccessReportDTO();
                dto.setDocument(doc);
                dto.setEmployeeCode(finalEmployeeCode);
                // Intentar obtener nombre y apellido del servicio de empleados
                try {
                    Map<String, Object> employeeInfo = employeeServicePort.getEmployeeByDocument(doc);
                    if (employeeInfo != null) {
                        dto.setEmployeeName((String) employeeInfo.get("firstname") + " " + (String) employeeInfo.get("lastname"));
                    }
                } catch (Exception e) {
                    System.err.println("Error al obtener información del empleado para documento " + doc + ": " + e.getMessage());
                    dto.setEmployeeName("Desconocido");
                }
                return dto;
            });

            EmployeeAccessReportDTO dto = employeeAccessMap.get(employeeDocument);
            if (record.getAccessType() == AccessRecord.AccessType.ENTRY) {
                dto.setEntryTime(record.getAccessTimestamp().format(TIME_FORMATTER));
            } else if (record.getAccessType() == AccessRecord.AccessType.EXIT) {
                dto.setExitTime(record.getAccessTimestamp().format(TIME_FORMATTER));
            }
        }

        return new ArrayList<>(employeeAccessMap.values());
    }
    
    /**
     * Obtiene o busca el employeeCode para un registro
     */
    private String getOrFetchEmployeeCode(AccessRecord record, String employeeDocument) {
        String employeeCode = record.getEmployeeCode();
        
        // Si el employeeCode es nulo o vacío, intentar obtenerlo del servicio de empleados
        if ((employeeCode == null || employeeCode.trim().isEmpty()) && employeeDocument != null && !employeeDocument.trim().isEmpty()) {
            try {
                Map<String, Object> employeeInfo = employeeServicePort.getEmployeeByDocument(employeeDocument);
                if (employeeInfo != null && employeeInfo.containsKey("employeeCode")) {
                    String fetchedCode = (String) employeeInfo.get("employeeCode");
                    // Actualizar el registro en la base de datos si se obtuvo el código
                    if (fetchedCode != null && !fetchedCode.trim().isEmpty()) {
                        record.setEmployeeCode(fetchedCode);
                        accessRecordRepositoryPort.save(record);
                        employeeCode = fetchedCode;
                    }
                }
            } catch (Exception e) {
                System.err.println("Error al obtener employeeCode para documento " + employeeDocument + ": " + e.getMessage());
            }
        }
        
        return employeeCode;
    }

    public List<EmployeeDetailedReportDTO> getEmployeeReportByDateRange(String document, LocalDateTime startDate, LocalDateTime endDate) {
        List<AccessRecord> records = accessRecordRepositoryPort.findByEmployeeIDAndDateRange(document, startDate, endDate);
        
        // Si no se encuentran registros por employeeID, intentar por employeeCode
        if (records.isEmpty()) {
            String employeeCode = null;
            try {
                Map<String, Object> employeeInfo = employeeServicePort.getEmployeeByDocument(document);
                if (employeeInfo != null && employeeInfo.containsKey("employeeCode")) {
                    employeeCode = (String) employeeInfo.get("employeeCode");
                }
            } catch (Exception e) {
                System.err.println("No se pudo obtener el código del empleado para documento " + document + ": " + e.getMessage());
            }

            if (employeeCode != null && !employeeCode.trim().isEmpty()) {
                records = accessRecordRepositoryPort.findByEmployeeCodeAndDateRange(employeeCode, startDate, endDate);
            }
        }

        // Obtener información del empleado una sola vez
        Map<String, Object> employeeInfo = employeeServicePort.getEmployeeByDocument(document);
        String employeeName = "Desconocido";
        String employeeCode = null;
        if (employeeInfo != null) {
            employeeName = (String) employeeInfo.get("firstname") + " " + (String) employeeInfo.get("lastname");
            employeeCode = (String) employeeInfo.get("employeeCode");
        }

        List<EmployeeDetailedReportDTO> report = new ArrayList<>();
        for (int i = 0; i < records.size(); i++) {
            AccessRecord entryRecord = records.get(i);
            if (entryRecord.getAccessType() == AccessRecord.AccessType.ENTRY) {
                EmployeeDetailedReportDTO reportItem = new EmployeeDetailedReportDTO();
                reportItem.setDocument(entryRecord.getEmployeeID());
                reportItem.setEmployeeCode(employeeCode != null ? employeeCode : entryRecord.getEmployeeCode());
                reportItem.setEmployeeName(employeeName);
                reportItem.setAccessDateTime(entryRecord.getAccessTimestamp());
                reportItem.setAccessType(entryRecord.getAccessType().name());
                reportItem.setAccessTime(entryRecord.getAccessTimestamp().format(TIME_FORMATTER));
                reportItem.setDate(entryRecord.getAccessTimestamp().format(DATE_FORMATTER));

                report.add(reportItem);
                
                // Buscar la salida correspondiente para el próximo registro
                for (int j = i + 1; j < records.size(); j++) {
                    AccessRecord currentRecord = records.get(j);
                    if (currentRecord.getAccessType() == AccessRecord.AccessType.EXIT &&
                        currentRecord.getAccessTimestamp().isAfter(entryRecord.getAccessTimestamp())) {
                        // Agregar salida como registro separado
                        EmployeeDetailedReportDTO exitItem = new EmployeeDetailedReportDTO();
                        exitItem.setDocument(currentRecord.getEmployeeID());
                        exitItem.setEmployeeCode(employeeCode != null ? employeeCode : currentRecord.getEmployeeCode());
                        exitItem.setEmployeeName(employeeName);
                        exitItem.setAccessDateTime(currentRecord.getAccessTimestamp());
                        exitItem.setAccessType(currentRecord.getAccessType().name());
                        exitItem.setAccessTime(currentRecord.getAccessTimestamp().format(TIME_FORMATTER));
                        exitItem.setDate(currentRecord.getAccessTimestamp().format(DATE_FORMATTER));
                        report.add(exitItem);
                        i = j;
                        break;
                    }
                }
            }
        }
        return report;
    }

    private String formatDuration(Duration duration) {
        long hours = duration.toHours();
        long minutes = duration.toMinutes() % 60;
        long seconds = duration.getSeconds() % 60;
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }
}

