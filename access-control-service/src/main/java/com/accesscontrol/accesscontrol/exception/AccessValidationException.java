package com.accesscontrol.accesscontrol.exception;

public class AccessValidationException extends RuntimeException {
    private String alertCode;
    
    public AccessValidationException(String message) {
        super(message);
    }
    
    public AccessValidationException(String message, String alertCode) {
        super(message);
        this.alertCode = alertCode;
    }
    
    public String getAlertCode() {
        return alertCode;
    }
}





