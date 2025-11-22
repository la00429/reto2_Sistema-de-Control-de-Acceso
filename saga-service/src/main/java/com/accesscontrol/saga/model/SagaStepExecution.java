package com.accesscontrol.saga.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entidad para persistir el estado de cada paso de una Saga
 */
@Entity
@Table(name = "saga_step_execution")
public class SagaStepExecution {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "saga_execution_id", nullable = false)
    private SagaExecution sagaExecution;
    
    @Column(nullable = false, length = 100)
    private String stepName;
    
    @Column(nullable = false, length = 50)
    private String serviceTarget; // employee-service, access-control-service, etc.
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StepStatus status; // PENDING, COMPLETED, FAILED, COMPENSATED
    
    @Column(columnDefinition = "TEXT")
    private String requestPayload; // JSON con los datos enviados
    
    @Column(columnDefinition = "TEXT")
    private String responsePayload; // JSON con la respuesta recibida
    
    @Column(columnDefinition = "TEXT")
    private String errorMessage;
    
    @Column(columnDefinition = "TEXT")
    private String compensationAction; // Nombre de la acción de compensación
    
    @Column(nullable = false)
    private LocalDateTime startedAt;
    
    private LocalDateTime completedAt;
    
    private Long durationMillis; // Duración en milisegundos

    @PrePersist
    protected void onCreate() {
        startedAt = LocalDateTime.now();
        if (status == null) {
            status = StepStatus.PENDING;
        }
    }

    public enum StepStatus {
        PENDING,
        IN_PROGRESS,
        COMPLETED,
        FAILED,
        COMPENSATING,
        COMPENSATED
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public SagaExecution getSagaExecution() {
        return sagaExecution;
    }

    public void setSagaExecution(SagaExecution sagaExecution) {
        this.sagaExecution = sagaExecution;
    }

    public String getStepName() {
        return stepName;
    }

    public void setStepName(String stepName) {
        this.stepName = stepName;
    }

    public String getServiceTarget() {
        return serviceTarget;
    }

    public void setServiceTarget(String serviceTarget) {
        this.serviceTarget = serviceTarget;
    }

    public StepStatus getStatus() {
        return status;
    }

    public void setStatus(StepStatus status) {
        this.status = status;
    }

    public String getRequestPayload() {
        return requestPayload;
    }

    public void setRequestPayload(String requestPayload) {
        this.requestPayload = requestPayload;
    }

    public String getResponsePayload() {
        return responsePayload;
    }

    public void setResponsePayload(String responsePayload) {
        this.responsePayload = responsePayload;
        if (completedAt == null && status == StepStatus.COMPLETED) {
            completedAt = LocalDateTime.now();
            if (startedAt != null) {
                durationMillis = java.time.Duration.between(startedAt, completedAt).toMillis();
            }
        }
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getCompensationAction() {
        return compensationAction;
    }

    public void setCompensationAction(String compensationAction) {
        this.compensationAction = compensationAction;
    }

    public LocalDateTime getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(LocalDateTime startedAt) {
        this.startedAt = startedAt;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }

    public Long getDurationMillis() {
        return durationMillis;
    }

    public void setDurationMillis(Long durationMillis) {
        this.durationMillis = durationMillis;
    }
}


