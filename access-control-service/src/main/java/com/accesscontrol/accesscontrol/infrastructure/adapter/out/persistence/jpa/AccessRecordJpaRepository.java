package com.accesscontrol.accesscontrol.infrastructure.adapter.out.persistence.jpa;

import com.accesscontrol.accesscontrol.model.AccessRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repositorio JPA
 * Arquitectura Hexagonal: Infrastructure Layer - Implementación técnica
 */
@Repository
public interface AccessRecordJpaRepository extends JpaRepository<AccessRecord, Long> {
    List<AccessRecord> findByEmployeeID(String employeeID);
    List<AccessRecord> findByEmployeeCode(String employeeCode);
    List<AccessRecord> findByAccessTimestampBetween(LocalDateTime start, LocalDateTime end);
    
    @Query("SELECT ar FROM AccessRecord ar WHERE ar.employeeID = :employeeID AND ar.accessTimestamp BETWEEN :start AND :end")
    List<AccessRecord> findByEmployeeIDAndDateRange(@Param("employeeID") String employeeID, 
                                                      @Param("start") LocalDateTime start, 
                                                      @Param("end") LocalDateTime end);
    
    @Query("SELECT ar FROM AccessRecord ar WHERE ar.employeeCode = :employeeCode AND ar.accessTimestamp BETWEEN :start AND :end")
    List<AccessRecord> findByEmployeeCodeAndDateRange(@Param("employeeCode") String employeeCode, 
                                                       @Param("start") LocalDateTime start, 
                                                       @Param("end") LocalDateTime end);
    
    @Query("SELECT ar FROM AccessRecord ar WHERE ar.employeeID = :employeeID ORDER BY ar.accessTimestamp DESC")
    List<AccessRecord> findLatestByEmployeeID(@Param("employeeID") String employeeID);
    
    @Query("SELECT ar FROM AccessRecord ar WHERE ar.employeeCode = :employeeCode ORDER BY ar.accessTimestamp DESC")
    List<AccessRecord> findLatestByEmployeeCode(@Param("employeeCode") String employeeCode);
}

