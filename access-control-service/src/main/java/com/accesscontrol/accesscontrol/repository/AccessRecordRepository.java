package com.accesscontrol.accesscontrol.repository;

import com.accesscontrol.accesscontrol.model.AccessRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AccessRecordRepository extends JpaRepository<AccessRecord, Long> {
    List<AccessRecord> findByEmployeeId(Long employeeId);
    List<AccessRecord> findByEmployeeCode(String employeeCode);
    List<AccessRecord> findByAccessTimestampBetween(LocalDateTime start, LocalDateTime end);
    
    @Query("SELECT ar FROM AccessRecord ar WHERE ar.employeeId = :employeeId AND ar.accessTimestamp BETWEEN :start AND :end")
    List<AccessRecord> findByEmployeeIdAndDateRange(@Param("employeeId") Long employeeId, 
                                                     @Param("start") LocalDateTime start, 
                                                     @Param("end") LocalDateTime end);
    
    @Query("SELECT ar FROM AccessRecord ar WHERE ar.employeeCode = :employeeCode AND ar.accessTimestamp BETWEEN :start AND :end")
    List<AccessRecord> findByEmployeeCodeAndDateRange(@Param("employeeCode") String employeeCode, 
                                                       @Param("start") LocalDateTime start, 
                                                       @Param("end") LocalDateTime end);
    
    @Query("SELECT ar FROM AccessRecord ar WHERE ar.employeeCode = :employeeCode ORDER BY ar.accessTimestamp DESC")
    List<AccessRecord> findLatestByEmployeeCode(@Param("employeeCode") String employeeCode);
    
    @Query("SELECT ar FROM AccessRecord ar WHERE ar.employeeID = :employeeID ORDER BY ar.accessTimestamp DESC")
    List<AccessRecord> findLatestByEmployeeID(@Param("employeeID") String employeeID);
    
    List<AccessRecord> findByEmployeeID(String employeeID);
    
    @Query("SELECT ar FROM AccessRecord ar WHERE ar.employeeId = :employeeId ORDER BY ar.accessTimestamp DESC")
    List<AccessRecord> findLatestByEmployeeId(@Param("employeeId") Long employeeId);
    
    @Query("SELECT ar FROM AccessRecord ar WHERE ar.employeeCode = :employeeCode AND ar.accessType = :accessType ORDER BY ar.accessTimestamp DESC")
    List<AccessRecord> findLatestByEmployeeCodeAndType(@Param("employeeCode") String employeeCode, 
                                                         @Param("accessType") AccessRecord.AccessType accessType);
}

