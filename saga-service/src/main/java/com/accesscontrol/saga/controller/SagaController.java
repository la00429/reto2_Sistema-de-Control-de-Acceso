package com.accesscontrol.saga.controller;

import com.accesscontrol.saga.orchestrator.AccessRegistrationSaga;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/saga")
@CrossOrigin(origins = "*")
@Tag(name = "SAGA", description = "API para orquestación de transacciones distribuidas")
public class SagaController {
    
    @Autowired
    private AccessRegistrationSaga accessRegistrationSaga;

    @Operation(summary = "Ejecutar SAGA de registro de acceso", description = "Orquesta una transacción distribuida para registrar el acceso de un empleado")
    @ApiResponse(responseCode = "200", description = "SAGA ejecutada exitosamente")
    @PostMapping("/access-registration")
    public ResponseEntity<Map<String, String>> executeAccessRegistration(
            @RequestBody Map<String, String> request) {
        
        String employeeDocument = request.get("employeeDocument");
        String accessType = request.get("accessType");
        
        String result = accessRegistrationSaga.executeAccessRegistration(employeeDocument, accessType);
        
        return ResponseEntity.ok(Map.of("result", result));
    }
}

