package com.accesscontrol.login.service;

import org.springframework.stereotype.Component;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Rastrea intentos de login de usuarios no registrados
 * Almacena en memoria (en producción usar Redis o similar)
 */
@Component
public class LoginAttemptTracker {
    
    private static final int MAX_UNREGISTERED_ATTEMPTS = 2;
    private final Map<String, Integer> unregisteredAttempts = new ConcurrentHashMap<>();
    private final Map<String, LocalDateTime> lastAttemptTime = new ConcurrentHashMap<>();
    
    public void recordUnregisteredAttempt(String username) {
        unregisteredAttempts.put(username, unregisteredAttempts.getOrDefault(username, 0) + 1);
        lastAttemptTime.put(username, LocalDateTime.now());
    }
    
    public boolean shouldAlertUnregisteredAttempt(String username) {
        int attempts = unregisteredAttempts.getOrDefault(username, 0);
        return attempts >= MAX_UNREGISTERED_ATTEMPTS;
    }
    
    public int getUnregisteredAttempts(String username) {
        return unregisteredAttempts.getOrDefault(username, 0);
    }
    
    public void clearUnregisteredAttempts(String username) {
        unregisteredAttempts.remove(username);
        lastAttemptTime.remove(username);
    }
    
    public LocalDateTime getLastAttemptTime(String username) {
        return lastAttemptTime.get(username);
    }
}




