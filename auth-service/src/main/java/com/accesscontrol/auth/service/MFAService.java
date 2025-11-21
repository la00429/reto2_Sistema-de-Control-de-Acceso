package com.accesscontrol.auth.service;

import com.accesscontrol.auth.model.MFAToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Servicio de Autenticación Multifactor (MFA)
 * Genera y valida códigos de 6 dígitos
 */
@Service
public class MFAService {
    
    private static final Logger logger = LoggerFactory.getLogger(MFAService.class);
    
    private static final int TOKEN_LENGTH = 6;
    private static final int TOKEN_EXPIRATION_MINUTES = 5;
    private final Map<String, MFAToken> mfaTokens = new ConcurrentHashMap<>();
    private final SecureRandom random = new SecureRandom();

    /**
     * Genera un código MFA de 6 dígitos para un usuario
     */
    public String generateMFAToken(String username) {
        String token = generateRandomToken();
        MFAToken mfaToken = new MFAToken(username, token);
        mfaToken.setExpiresAt(LocalDateTime.now().plusMinutes(TOKEN_EXPIRATION_MINUTES));
        
        mfaTokens.put(username, mfaToken);
        
        // En producción, enviar por email/SMS
        logger.info("MFAService: Token generado para {}: {}", username, token);
        
        return token;
    }

    /**
     * Valida el código MFA
     */
    public boolean validateMFAToken(String username, String token) {
        logger.info("MFAService: Validando token para usuario: {}", username);
        logger.info("MFAService: Token recibido: {}", token);
        
        MFAToken mfaToken = mfaTokens.get(username);
        
        if (mfaToken == null) {
            logger.warn("MFAService: No se encontró token MFA para usuario: {}", username);
            return false;
        }
        
        logger.debug("MFAService: Token almacenado: {}", mfaToken.getToken());
        logger.debug("MFAService: Token expirado: {}", mfaToken.isExpired());
        logger.debug("MFAService: Token ya verificado: {}", mfaToken.isVerified());
        
        if (mfaToken.isExpired()) {
            logger.warn("MFAService: Token expirado para usuario {}. Eliminando...", username);
            mfaTokens.remove(username);
            return false;
        }
        
        if (mfaToken.isVerified()) {
            logger.warn("MFAService: Token ya utilizado anteriormente para usuario {}", username);
            return false; // Token ya usado
        }
        
        boolean isValid = mfaToken.getToken().equals(token);
        logger.info("MFAService: Comparación de tokens - Válido: {}", isValid);
        
        if (isValid) {
            mfaToken.setVerified(true);
            logger.info("MFAService: Token validado exitosamente para usuario {}", username);
        } else {
            logger.warn("MFAService: Token no coincide para usuario {}. Esperado: {}, Recibido: {}",
                username, mfaToken.getToken(), token);
        }
        
        return isValid;
    }

    /**
     * Verifica si un usuario tiene un token MFA activo
     */
    public boolean hasActiveToken(String username) {
        MFAToken mfaToken = mfaTokens.get(username);
        return mfaToken != null && !mfaToken.isExpired() && !mfaToken.isVerified();
    }

    /**
     * Elimina un token MFA (después de uso exitoso)
     */
    public void removeToken(String username) {
        mfaTokens.remove(username);
    }

    private String generateRandomToken() {
        int min = 1;
        for (int i = 1; i < TOKEN_LENGTH; i++) {
            min *= 10;
        }
        int maxExclusive = min * 10;
        int token = min + random.nextInt(maxExclusive - min);
        return String.valueOf(token);
    }
}


