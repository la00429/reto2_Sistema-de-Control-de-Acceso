package com.accesscontrol.saga.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entidad para persistir el estado de una ejecución de Saga
 */
@Entity
@Table(name = "saga_execution")
public class SagaExecution {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false, length = 100)
    private String sagaId;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SagaState state;
    
    @Column(nullable = false, length = 100)
    private String sagaType; // ACCESS_REGISTRATION, EMPLOYEE_CREATION, etc.
    
    @Column(columnDefinition = "TEXT")
    private String payload; // JSON con los datos de la saga
    
    @Column(columnDefinition = "TEXT")
    private String errorMessage;
    
    @Column(nullable = false)
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
    
    private LocalDateTime completedAt;
    
    @OneToMany(mappedBy = "sagaExecution", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<SagaStepExecution> steps = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (state == null) {
            state = SagaState.PENDING;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
        if (state == SagaState.COMPLETED || state == SagaState.COMPENSATED || state == SagaState.FAILED) {
            completedAt = LocalDateTime.now();
        }
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSagaId() {
        return sagaId;
    }

    public void setSagaId(String sagaId) {
        this.sagaId = sagaId;
    }

    public SagaState getState() {
        return state;
    }

    public void setState(SagaState state) {
        this.state = state;
    }

    public String getSagaType() {
        return sagaType;
    }

    public void setSagaType(String sagaType) {
        this.sagaType = sagaType;
    }

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }

    public List<SagaStepExecution> getSteps() {
        return steps;
    }

    public void setSteps(List<SagaStepExecution> steps) {
        this.steps = steps;
    }

    public void addStep(SagaStepExecution step) {
        steps.add(step);
        step.setSagaExecution(this);
    }
}


