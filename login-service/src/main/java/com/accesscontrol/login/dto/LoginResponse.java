package com.accesscontrol.login.dto;

public class LoginResponse {
    private boolean success;
    private String message;
    private String username;
    private String token;
    private boolean requiresMFA;  // Indica si requiere autenticación multifactor

    public LoginResponse() {}

    public LoginResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
        this.requiresMFA = false; // Por defecto no requiere MFA
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public boolean isRequiresMFA() {
        return requiresMFA;
    }

    public void setRequiresMFA(boolean requiresMFA) {
        this.requiresMFA = requiresMFA;
    }
}

