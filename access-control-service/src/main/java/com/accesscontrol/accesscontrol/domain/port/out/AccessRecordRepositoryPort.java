package com.accesscontrol.accesscontrol.domain.port.out;

import com.accesscontrol.accesscontrol.domain.model.AccessRecord;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Puerto de salida - Repository
 * Arquitectura Hexagonal: Define el contrato para persistencia
 */
public interface AccessRecordRepositoryPort {
    AccessRecord save(AccessRecord accessRecord);
    Optional<AccessRecord> findById(Long id);
    List<AccessRecord> findAll();
    List<AccessRecord> findByEmployeeID(String employeeID);
    List<AccessRecord> findByEmployeeCode(String employeeCode);
    List<AccessRecord> findByEmployeeId(Long employeeId);
    List<AccessRecord> findByAccessTimestampBetween(LocalDateTime start, LocalDateTime end);
    List<AccessRecord> findByEmployeeIDAndDateRange(String employeeID, LocalDateTime start, LocalDateTime end);
    List<AccessRecord> findByEmployeeCodeAndDateRange(String employeeCode, LocalDateTime start, LocalDateTime end);
    List<AccessRecord> findByEmployeeIdAndDateRange(Long employeeId, LocalDateTime start, LocalDateTime end);
    List<AccessRecord> findLatestByEmployeeID(String employeeID);
    List<AccessRecord> findLatestByEmployeeCode(String employeeCode);
}



