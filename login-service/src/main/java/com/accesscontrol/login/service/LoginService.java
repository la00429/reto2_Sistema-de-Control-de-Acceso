package com.accesscontrol.login.service;

import com.accesscontrol.login.dto.LoginRequest;
import com.accesscontrol.login.dto.LoginResponse;
import com.accesscontrol.login.dto.RegisterRequest;
import com.accesscontrol.login.model.User;
import com.accesscontrol.login.repository.UserRepository;
import com.accesscontrol.login.dto.AlertEvent;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class LoginService {
    
    private static final Logger logger = LoggerFactory.getLogger(LoginService.class);
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private RestTemplate restTemplate;
    
    @Value("${auth.service.url:http://localhost:8085}")
    private String authServiceUrl;
    
    @Autowired
    private LoginAttemptTracker attemptTracker;
    
    @Autowired
    private RabbitTemplate rabbitTemplate;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    private static final int MAX_FAILED_ATTEMPTS = 3;  // Según especificación: más de 3 intentos
    private static final long LOCKOUT_DURATION_MINUTES = 10;  // Según especificación: 10 minutos
    private static final String ALERT_QUEUE = "alert.queue";

    public LoginResponse authenticate(LoginRequest request) {
        logger.debug("Iniciando autenticación para usuario: {}", request.getUsername());
        Optional<User> userOpt = userRepository.findByUsername(request.getUsername());
        
        if (userOpt.isEmpty()) {
            logger.warn("Usuario no encontrado en la base de datos: {}", request.getUsername());
            // Usuario no registrado - rastrear intentos
            attemptTracker.recordUnregisteredAttempt(request.getUsername());
            
            if (attemptTracker.shouldAlertUnregisteredAttempt(request.getUsername())) {
                sendUnregisteredUserAlert(request.getUsername());
            }
            
            return new LoginResponse(false, "Invalid credentials");
        }
        
        User user = userOpt.get();
        logger.debug("Usuario encontrado. Estado: {}", user.getStatus());
        
        // Limpiar intentos de usuario no registrado si el usuario existe
        attemptTracker.clearUnregisteredAttempts(request.getUsername());
        
        // Verificar si el usuario está bloqueado temporalmente
        if (isUserLocked(user)) {
            long minutesRemaining = calculateMinutesRemaining(user.getLockoutUntil());
            logger.warn("Usuario bloqueado. Minutos restantes: {}", minutesRemaining);
            return new LoginResponse(false, 
                String.format("Account is locked. Please try again in %d minute(s).", minutesRemaining));
        }
        
        // Validar contraseña con BCrypt
        logger.debug("Validando contraseña...");
        boolean passwordValid = validatePassword(request.getPassword(), user.getPasswordHash(), user);
        logger.debug("Contraseña válida: {}", passwordValid);
        
        if (!passwordValid) {
            logger.warn("Contraseña inválida para usuario: {}. Intentos fallidos: {}", 
                request.getUsername(), user.getFailedLoginAttempts());
            handleFailedLogin(user);
            return new LoginResponse(false, "Invalid credentials");
        }
        
        // Solicitar código MFA al servicio de autenticación
        try {
            logger.info("Solicitando código MFA para usuario: {}", user.getUsername());
            String mfaToken = requestMFATokenFromAuthService(user.getUsername());
            
            if (mfaToken == null || mfaToken.isEmpty()) {
                logger.error("Error generando MFA token: No se recibió token del Auth Service");
                return new LoginResponse(false, 
                    "Error generando código MFA. Por favor, verifica que el servicio de autenticación esté disponible.");
            }
            
            // Imprimir código MFA en consola para desarrollo
            logger.info("========================================");
            logger.info("MFA Token para {}: {}", user.getUsername(), mfaToken);
            logger.info("========================================");
            
            // Actualizar último login y resetear intentos fallidos
            user.setLastLogin(LocalDateTime.now());
            user.setFailedLoginAttempts(0);
            user.setUpdatedAt(LocalDateTime.now());
            userRepository.save(user);
            
            LoginResponse response = new LoginResponse(true, "MFA token sent. Please verify.");
            response.setUsername(user.getUsername());
            response.setToken(mfaToken); // Token MFA temporal
            response.setRequiresMFA(true);
            
            // Log detallado para debugging
            logger.info("Login exitoso para usuario: {}. Esperando verificación MFA.", user.getUsername());
            logger.debug("LoginResponse creado - Success: {}, RequiresMFA: {}, Token: {}, Username: {}, Message: {}", 
                response.isSuccess(), 
                response.isRequiresMFA(), 
                response.getToken() != null ? "Presente" : "Null",
                response.getUsername(),
                response.getMessage());
            
            return response;
        } catch (RuntimeException e) {
            logger.error("Error generando MFA token para usuario: {}. Error: {}", 
                user.getUsername(), e.getMessage(), e);
            return new LoginResponse(false, "Error: " + e.getMessage());
        } catch (Exception e) {
            logger.error("Error inesperado generando MFA token para usuario: {}", user.getUsername(), e);
            return new LoginResponse(false, 
                "Error inesperado al generar código MFA. Por favor, intenta nuevamente.");
        }
    }

    public LoginResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            return new LoginResponse(false, "Username already exists");
        }
        
        if (userRepository.existsByEmail(request.getEmail())) {
            return new LoginResponse(false, "Email already exists");
        }
        
        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setRole(request.getRole() != null ? request.getRole() : "USER");
        user.setStatus("ACTIVE");
        user.setFailedLoginAttempts(0);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        
        userRepository.save(user);
        logger.info("Usuario registrado exitosamente: {}", request.getUsername());
        
        return new LoginResponse(true, "User registered successfully");
    }

    private void handleFailedLogin(User user) {
        int attempts = user.getFailedLoginAttempts() != null ? user.getFailedLoginAttempts() : 0;
        attempts++;
        user.setFailedLoginAttempts(attempts);
        
        // Si supera el límite de intentos, bloquear por 10 minutos
        if (attempts > MAX_FAILED_ATTEMPTS) {
            user.setStatus("LOCKED");
            user.setLockoutUntil(LocalDateTime.now().plusMinutes(LOCKOUT_DURATION_MINUTES));
            sendExceededAttemptsAlert(user.getUsername());
        }
        
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
    }
    
    private boolean isUserLocked(User user) {
        if (!"LOCKED".equals(user.getStatus())) {
            return false;
        }
        
        // Verificar si el bloqueo ha expirado
        if (user.getLockoutUntil() != null && LocalDateTime.now().isAfter(user.getLockoutUntil())) {
            // Desbloquear usuario
            user.setStatus("ACTIVE");
            user.setFailedLoginAttempts(0);
            user.setLockoutUntil(null);
            userRepository.save(user);
            return false;
        }
        
        return true;
    }
    
    private long calculateMinutesRemaining(LocalDateTime lockoutUntil) {
        if (lockoutUntil == null) {
            return 0;
        }
        long seconds = java.time.Duration.between(LocalDateTime.now(), lockoutUntil).getSeconds();
        return Math.max(0, seconds / 60);
    }
    
    private void sendUnregisteredUserAlert(String username) {
        try {
            AlertEvent alertEvent = new AlertEvent();
            alertEvent.setCode("LOGIN_USR_NOT_REGISTERED");
            alertEvent.setDescription("Intento de autenticación de usuario no registrado: " + username + 
                " (2 o más intentos)");
            alertEvent.setUsername(username);
            alertEvent.setTimestamp(LocalDateTime.now().toString());
            
            rabbitTemplate.convertAndSend(ALERT_QUEUE, alertEvent);
            logger.info("Alerta enviada: Usuario no registrado intentando autenticarse - {}", username);
        } catch (Exception e) {
            logger.error("Error enviando alerta de usuario no registrado: {}", e.getMessage(), e);
        }
    }
    
    private void sendExceededAttemptsAlert(String username) {
        try {
            AlertEvent alertEvent = new AlertEvent();
            alertEvent.setCode("LOGIN_USR_ATTEMPS_EXCEEDED");
            alertEvent.setDescription("Usuario registrado con más de 3 intentos fallidos: " + username);
            alertEvent.setUsername(username);
            alertEvent.setTimestamp(LocalDateTime.now().toString());
            
            rabbitTemplate.convertAndSend(ALERT_QUEUE, alertEvent);
            logger.warn("Alerta enviada: Usuario con intentos excedidos - {}", username);
        } catch (Exception e) {
            logger.error("Error enviando alerta de intentos excedidos: {}", e.getMessage(), e);
        }
    }

    private String requestMFATokenFromAuthService(String username) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            String requestBody = "{\"username\":\"" + username + "\"}";
            HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);
            
            String url = authServiceUrl + "/api/auth/mfa/generate";
            logger.info("Solicitando MFA token desde: {}", url);
            
            @SuppressWarnings("unchecked")
            ResponseEntity<Map<String, Object>> response = (ResponseEntity<Map<String, Object>>) (ResponseEntity<?>) restTemplate.postForEntity(
                url,
                entity,
                Map.class
            );
            
            logger.debug("Respuesta recibida del Auth Service. Status: {}", response.getStatusCode());
            
            if (response.getStatusCode().is2xxSuccessful()) {
                Map<String, Object> body = response.getBody();
                logger.debug("Body de respuesta: {}", body);
                if (body != null && body.containsKey("mfaToken")) {
                    String mfaToken = (String) body.get("mfaToken");
                    logger.info("MFA token recibido exitosamente");
                    return mfaToken;
                } else {
                    logger.error("Respuesta del Auth Service no contiene mfaToken. Body: {}", body);
                    return null;
                }
            } else {
                logger.error("Error al solicitar MFA token. Status: {}, Body: {}", 
                    response.getStatusCode(), response.getBody());
                return null;
            }
        } catch (org.springframework.web.client.ResourceAccessException e) {
            logger.error("Error de conexión al Auth Service ({}). Verifica que el servicio esté ejecutándose.", 
                authServiceUrl, e);
            throw new RuntimeException("El servicio de autenticación no está disponible. Error: " + e.getMessage(), e);
        } catch (org.springframework.web.client.HttpClientErrorException e) {
            logger.error("Error HTTP al solicitar MFA token. Status: {}, Response: {}", 
                e.getStatusCode(), e.getResponseBodyAsString(), e);
            throw new RuntimeException("Error al solicitar MFA token: " + e.getMessage(), e);
        } catch (Exception e) {
            logger.error("Excepción inesperada al solicitar MFA token: {}", e.getMessage(), e);
            throw new RuntimeException("Error inesperado al solicitar MFA token: " + e.getMessage(), e);
        }
    }

    private boolean validatePassword(String rawPassword, String hashedPassword, User user) {
        // Si el hash es texto plano (migración de usuarios antiguos), comparar directamente
        // Luego actualizar el hash con BCrypt
        if (hashedPassword != null && hashedPassword.length() < 60) {
            // Probablemente es texto plano, verificar y migrar
            if (rawPassword.equals(hashedPassword)) {
                logger.warn("Contraseña en texto plano detectada para usuario: {}. Migrando a BCrypt...", user.getUsername());
                // Migrar contraseña a BCrypt
                user.setPasswordHash(passwordEncoder.encode(rawPassword));
                user.setUpdatedAt(LocalDateTime.now());
                userRepository.save(user);
                logger.info("Contraseña migrada a BCrypt exitosamente para usuario: {}", user.getUsername());
                return true;
            }
            return false;
        }
        // Validar con BCrypt
        boolean matches = passwordEncoder.matches(rawPassword, hashedPassword);
        if (!matches) {
            logger.debug("La contraseña no coincide con el hash BCrypt");
        }
        return matches;
    }
}

