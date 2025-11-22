package com.accesscontrol.saga.service;

import com.accesscontrol.saga.model.SagaExecution;
import com.accesscontrol.saga.model.SagaState;
import com.accesscontrol.saga.model.SagaStepExecution;
import com.accesscontrol.saga.repository.SagaExecutionRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Servicio para gestionar ejecuciones de Saga
 */
@Service
@Transactional
public class SagaExecutionService {
    
    @Autowired
    private SagaExecutionRepository sagaExecutionRepository;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    private final Counter sagaStartedCounter;
    private final Counter sagaCompletedCounter;
    private final Counter sagaCompensatedCounter;
    private final Counter sagaFailedCounter;
    private final Timer sagaExecutionTimer;
    
    public SagaExecutionService(MeterRegistry meterRegistry) {
        this.sagaStartedCounter = Counter.builder("saga.started")
                .description("Number of sagas started")
                .register(meterRegistry);
        
        this.sagaCompletedCounter = Counter.builder("saga.completed")
                .description("Number of sagas completed")
                .tag("status", "success")
                .register(meterRegistry);
        
        this.sagaCompensatedCounter = Counter.builder("saga.compensated")
                .description("Number of sagas compensated")
                .tag("status", "compensated")
                .register(meterRegistry);
        
        this.sagaFailedCounter = Counter.builder("saga.failed")
                .description("Number of sagas failed")
                .tag("status", "failed")
                .register(meterRegistry);
        
        this.sagaExecutionTimer = Timer.builder("saga.execution.time")
                .description("Saga execution time")
                .register(meterRegistry);
    }
    
    public SagaExecution createSaga(String sagaType, Map<String, Object> payload) {
        SagaExecution saga = new SagaExecution();
        saga.setSagaId(UUID.randomUUID().toString());
        saga.setSagaType(sagaType);
        saga.setState(SagaState.PENDING);
        
        try {
            saga.setPayload(objectMapper.writeValueAsString(payload));
        } catch (JsonProcessingException e) {
            saga.setPayload(payload.toString());
        }
        
        saga = sagaExecutionRepository.save(saga);
        sagaStartedCounter.increment();
        return saga;
    }
    
    public Optional<SagaExecution> getSagaBySagaId(String sagaId) {
        return sagaExecutionRepository.findBySagaId(sagaId);
    }
    
    public SagaExecution updateSagaState(String sagaId, SagaState newState) {
        Optional<SagaExecution> optional = sagaExecutionRepository.findBySagaId(sagaId);
        if (optional.isPresent()) {
            SagaExecution saga = optional.get();
            saga.setState(newState);
            
            if (newState == SagaState.COMPLETED) {
                sagaCompletedCounter.increment();
                sagaExecutionTimer.record(java.time.Duration.between(
                    saga.getCreatedAt(), LocalDateTime.now()));
            } else if (newState == SagaState.COMPENSATED) {
                sagaCompensatedCounter.increment();
            } else if (newState == SagaState.FAILED) {
                sagaFailedCounter.increment();
            }
            
            return sagaExecutionRepository.save(saga);
        }
        return null;
    }
    
    public SagaExecution addStep(String sagaId, SagaStepExecution step) {
        Optional<SagaExecution> optional = sagaExecutionRepository.findBySagaId(sagaId);
        if (optional.isPresent()) {
            SagaExecution saga = optional.get();
            saga.addStep(step);
            return sagaExecutionRepository.save(saga);
        }
        return null;
    }
    
    public SagaExecution markSagaAsInProgress(String sagaId) {
        return updateSagaState(sagaId, SagaState.IN_PROGRESS);
    }
    
    public SagaExecution markSagaAsCompleted(String sagaId) {
        return updateSagaState(sagaId, SagaState.COMPLETED);
    }
    
    public SagaExecution markSagaAsCompensating(String sagaId) {
        return updateSagaState(sagaId, SagaState.COMPENSATING);
    }
    
    public SagaExecution markSagaAsCompensated(String sagaId) {
        return updateSagaState(sagaId, SagaState.COMPENSATED);
    }
    
    public SagaExecution markSagaAsFailed(String sagaId, String errorMessage) {
        Optional<SagaExecution> optional = sagaExecutionRepository.findBySagaId(sagaId);
        if (optional.isPresent()) {
            SagaExecution saga = optional.get();
            saga.setState(SagaState.FAILED);
            saga.setErrorMessage(errorMessage);
            sagaFailedCounter.increment();
            return sagaExecutionRepository.save(saga);
        }
        return null;
    }
}

