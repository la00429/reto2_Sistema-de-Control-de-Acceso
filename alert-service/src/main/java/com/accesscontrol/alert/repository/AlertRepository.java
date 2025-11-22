package com.accesscontrol.alert.repository;

import com.accesscontrol.alert.model.Alert;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AlertRepository extends JpaRepository<Alert, Long> {
    List<Alert> findByCode(String code);
    List<Alert> findByUsername(String username);
    List<Alert> findByEmployeeCode(String employeeCode);
    List<Alert> findByTimestampBetween(LocalDateTime start, LocalDateTime end);
    
    @Query("SELECT a FROM Alert a WHERE a.username = :username AND a.code = :code AND a.timestamp >= :since")
    List<Alert> findRecentAlertsByUserAndCode(@Param("username") String username, 
                                               @Param("code") String code, 
                                               @Param("since") LocalDateTime since);
}




