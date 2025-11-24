package com.accesscontrol.saga.model;

public class SagaStep {
    private String sagaId;
    private String stepName;
    private boolean success;
    private String errorMessage;
    private String compensationAction;

    public String getSagaId() {
        return sagaId;
    }

    public void setSagaId(String sagaId) {
        this.sagaId = sagaId;
    }

    public String getStepName() {
        return stepName;
    }

    public void setStepName(String stepName) {
        this.stepName = stepName;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
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
}





