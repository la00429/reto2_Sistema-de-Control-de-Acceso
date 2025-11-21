package com.accesscontrol.login.controller;

import com.accesscontrol.login.dto.LoginRequest;
import com.accesscontrol.login.dto.LoginResponse;
import com.accesscontrol.login.dto.RegisterRequest;
import com.accesscontrol.login.service.LoginService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/login")
@Tag(name = "Login", description = "API para autenticación y registro de usuarios")
public class LoginController {
    
    private static final Logger logger = LoggerFactory.getLogger(LoginController.class);
    private final LoginService loginService;

    public LoginController(LoginService loginService) {
        this.loginService = loginService;
    }

    @Operation(
        summary = "Autenticar usuario",
        description = "Autentica un usuario con credenciales y genera código MFA"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Autenticación exitosa, código MFA generado",
            content = @Content(schema = @Schema(implementation = LoginResponse.class))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Credenciales inválidas o usuario bloqueado",
            content = @Content
        )
    })
    @PostMapping("/authuser")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        logger.info("========================================");
        logger.info("LoginController: Petición recibida para usuario: {}", request.getUsername());
        logger.info("========================================");
        try {
            LoginResponse response = loginService.authenticate(request);
            logger.info("LoginController: Respuesta generada - Success: {}, RequiresMFA: {}, Message: {}", 
                response.isSuccess(), response.isRequiresMFA(), response.getMessage());
            
            if (response.isSuccess()) {
                logger.info("LoginController: Token MFA presente: {}", response.getToken() != null);
                if (response.isRequiresMFA()) {
                    logger.info("LoginController: Respuesta exitosa con MFA requerido para usuario: {}", request.getUsername());
                }
            } else {
                logger.warn("Login fallido para usuario: {}. Razón: {}", request.getUsername(), response.getMessage());
            }
            
            // Log detallado de la respuesta antes de enviarla
            logger.info("LoginController: Enviando respuesta JSON - Success: {}, RequiresMFA: {}, Message: {}, Username: {}, Token: {}", 
                response.isSuccess(), 
                response.isRequiresMFA(), 
                response.getMessage(),
                response.getUsername(),
                response.getToken() != null ? "Presente (" + response.getToken().length() + " chars)" : "No presente");
            
            // Asegurar que el username esté establecido si no viene en la respuesta
            if (response.getUsername() == null || response.getUsername().isEmpty()) {
                response.setUsername(request.getUsername());
                logger.debug("Username establecido desde request: {}", request.getUsername());
            }
            
            // Log final antes de enviar
            logger.debug("LoginController: Preparando respuesta final - Success: {}, RequiresMFA: {}, Username: {}, Token length: {}", 
                response.isSuccess(), 
                response.isRequiresMFA(),
                response.getUsername(),
                response.getToken() != null ? response.getToken().length() : 0);
            
            ResponseEntity<LoginResponse> httpResponse = ResponseEntity.ok(response);
            logger.info("LoginController: Respuesta HTTP creada correctamente");
            return httpResponse;
        } catch (Exception e) {
            logger.error("Error inesperado en LoginController para usuario: {}", request.getUsername(), e);
            LoginResponse errorResponse = new LoginResponse(false, "Error interno del servidor: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @Operation(
        summary = "Registrar nuevo usuario",
        description = "Crea un nuevo usuario en el sistema"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201",
            description = "Usuario creado exitosamente",
            content = @Content(schema = @Schema(implementation = LoginResponse.class))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Datos inválidos o usuario ya existe",
            content = @Content
        )
    })
    @PostMapping("/createuser")
    public ResponseEntity<LoginResponse> register(@Valid @RequestBody RegisterRequest request) {
        LoginResponse response = loginService.register(request);
        if (response.isSuccess()) {
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        }
        return ResponseEntity.badRequest().body(response);
    }
}

