package com.accesscontrol.login.config;

import com.accesscontrol.login.model.User;
import com.accesscontrol.login.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class DataInitializer implements CommandLineRunner {
    
    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);
    private static final String SEPARATOR = "========================================";
    private static final String ADMIN_USERNAME = "admin";
    private static final String ADMIN_PASSWORD = "admin";
    private static final String ADMIN_EMAIL = "admin@accesscontrol.com";
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    
    public DataInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }
    
    @Override
    public void run(String... args) throws Exception {
        logger.info(SEPARATOR);
        logger.info("DataInitializer: Verificando usuario admin...");
        logger.info(SEPARATOR);
        
        // Crear usuario admin por defecto si no existe
        if (!userRepository.existsByUsername(ADMIN_USERNAME)) {
            logger.info("DataInitializer: Usuario admin no existe. Creando...");
            User admin = new User();
            admin.setUsername(ADMIN_USERNAME);
            admin.setEmail(ADMIN_EMAIL);
            admin.setPasswordHash(passwordEncoder.encode(ADMIN_PASSWORD));
            admin.setRole("ADMIN");
            admin.setStatus("ACTIVE");
            admin.setFailedLoginAttempts(0);
            admin.setCreatedAt(LocalDateTime.now());
            admin.setUpdatedAt(LocalDateTime.now());
            
            userRepository.save(admin);
            logger.info(SEPARATOR);
            logger.info("Usuario admin creado por defecto");
            logger.info("Username: {}", ADMIN_USERNAME);
            logger.info("Password: {}", ADMIN_PASSWORD);
            logger.info(SEPARATOR);
        } else {
            logger.info(SEPARATOR);
            logger.info("Usuario admin ya existe en la base de datos");
            logger.info(SEPARATOR);
        }
    }
}

