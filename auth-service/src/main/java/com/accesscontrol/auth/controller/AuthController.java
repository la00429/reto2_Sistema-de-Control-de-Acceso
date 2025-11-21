package com.accesscontrol.auth.controller;

import com.accesscontrol.auth.dto.TokenRequest;
import com.accesscontrol.auth.dto.TokenResponse;
import com.accesscontrol.auth.service.JwtService;
import com.accesscontrol.auth.service.MFAService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "API para autenticación JWT y MFA")
public class AuthController {
    
    private final JwtService jwtService;
    private final MFAService mfaService;

    public AuthController(JwtService jwtService, MFAService mfaService) {
        this.jwtService = jwtService;
        this.mfaService = mfaService;
    }

    @Operation(summary = "Generar token JWT", description = "Genera un token JWT de acceso y refresh para un usuario")
    @ApiResponse(responseCode = "200", description = "Token generado exitosamente")
    @PostMapping("/generate-token")
    public ResponseEntity<TokenResponse> generateToken(@RequestBody TokenRequest request) {
        TokenResponse response = jwtService.generateToken(request.getUsername());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/validate-token")
    public ResponseEntity<Boolean> validateToken(@RequestHeader("Authorization") String token) {
        String jwt = token.replace("Bearer ", "");
        boolean isValid = jwtService.validateToken(jwt);
        return ResponseEntity.ok(isValid);
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<TokenResponse> refreshToken(@RequestBody String refreshToken) {
        TokenResponse response = jwtService.refreshToken(refreshToken);
        return ResponseEntity.ok(response);
    }

    // Endpoints MFA
    @Operation(summary = "Generar código MFA", description = "Genera un código de 6 dígitos para autenticación multifactor")
    @ApiResponse(responseCode = "200", description = "Código MFA generado exitosamente")
    @PostMapping("/mfa/generate")
    public ResponseEntity<Map<String, String>> generateMFAToken(@RequestBody TokenRequest request) {
        String token = mfaService.generateMFAToken(request.getUsername());
        return ResponseEntity.ok(Map.of("mfaToken", token, "message", "MFA token generated"));
    }

    @Operation(summary = "Validar código MFA", description = "Valida el código MFA y genera token JWT final")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "MFA validado exitosamente, token JWT generado"),
        @ApiResponse(responseCode = "200", description = "Código MFA inválido")
    })
    @PostMapping("/mfa/validate")
    public ResponseEntity<Map<String, Object>> validateMFAToken(@RequestBody Map<String, String> request) {
        String username = request.get("username");
        String token = request.get("token");
        
        System.out.println("========================================");
        System.out.println("AuthController: Validando MFA para usuario: " + username);
        System.out.println("AuthController: Token recibido: " + token);
        System.out.println("========================================");
        
        boolean isValid = mfaService.validateMFAToken(username, token);
        
        System.out.println("AuthController: Resultado de validación: " + isValid);
        
        if (isValid) {
            // Generar JWT después de validar MFA
            System.out.println("AuthController: Generando JWT para usuario: " + username);
            TokenResponse jwtResponse = jwtService.generateToken(username);
            mfaService.removeToken(username);
            
            System.out.println("AuthController: MFA validado exitosamente. JWT generado.");
            System.out.println("========================================");
            
            return ResponseEntity.ok(Map.of(
                "valid", true,
                "accessToken", jwtResponse.getAccessToken(),
                "refreshToken", jwtResponse.getRefreshToken()
            ));
        }
        
        System.out.println("AuthController: MFA inválido para usuario: " + username);
        System.out.println("========================================");
        
        return ResponseEntity.ok(Map.of("valid", false, "message", "Invalid MFA token"));
    }
}

