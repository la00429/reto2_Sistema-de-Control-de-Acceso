package com.accesscontrol.saga.model;

public enum SagaState {
    PENDING,
    IN_PROGRESS,
    COMPLETED,
    COMPENSATING,
    COMPENSATED,
    FAILED
}



