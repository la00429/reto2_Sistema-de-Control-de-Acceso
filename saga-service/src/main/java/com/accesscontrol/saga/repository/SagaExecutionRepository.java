package com.accesscontrol.saga.repository;

import com.accesscontrol.saga.model.SagaExecution;
import com.accesscontrol.saga.model.SagaState;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SagaExecutionRepository extends JpaRepository<SagaExecution, Long> {
    
    Optional<SagaExecution> findBySagaId(String sagaId);
    
    List<SagaExecution> findByState(SagaState state);
    
    List<SagaExecution> findBySagaType(String sagaType);
    
    @Query("SELECT s FROM SagaExecution s WHERE s.state IN :states AND s.createdAt >= :since")
    List<SagaExecution> findByStatesAndCreatedAfter(@Param("states") List<SagaState> states, @Param("since") LocalDateTime since);
    
    @Query("SELECT s FROM SagaExecution s WHERE s.state = :state AND s.updatedAt < :before")
    List<SagaExecution> findStaleExecutions(@Param("state") SagaState state, @Param("before") LocalDateTime before);
}



