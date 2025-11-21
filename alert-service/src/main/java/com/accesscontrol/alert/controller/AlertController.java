package com.accesscontrol.alert.controller;

import com.accesscontrol.alert.dto.AlertDTO;
import com.accesscontrol.alert.dto.CreateAlertRequest;
import com.accesscontrol.alert.service.AlertService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/alert")
@CrossOrigin(origins = "*")
@Tag(name = "Alert", description = "API para gestión de alertas del sistema")
public class AlertController {
    
    private final AlertService alertService;

    public AlertController(AlertService alertService) {
        this.alertService = alertService;
    }

    @PostMapping("/create")
    public ResponseEntity<AlertDTO> createAlert(@RequestBody CreateAlertRequest request) {
        AlertDTO alert = alertService.createAlert(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(alert);
    }

    @GetMapping("/all")
    public ResponseEntity<List<AlertDTO>> getAllAlerts() {
        List<AlertDTO> alerts = alertService.getAllAlerts();
        return ResponseEntity.ok(alerts);
    }

    @GetMapping("/code/{code}")
    public ResponseEntity<List<AlertDTO>> getAlertsByCode(@PathVariable String code) {
        List<AlertDTO> alerts = alertService.getAlertsByCode(code);
        return ResponseEntity.ok(alerts);
    }

    @GetMapping("/username/{username}")
    public ResponseEntity<List<AlertDTO>> getAlertsByUsername(@PathVariable String username) {
        List<AlertDTO> alerts = alertService.getAlertsByUsername(username);
        return ResponseEntity.ok(alerts);
    }

    // Endpoints según nomenclatura especificada
    @Operation(summary = "Alerta: Usuario no registrado", description = "Registra una alerta cuando un usuario no registrado intenta autenticarse")
    @ApiResponse(responseCode = "201", description = "Alerta creada exitosamente")
    @PostMapping("/usrnotregistattempt")
    public ResponseEntity<AlertDTO> userNotRegisteredAttempt(@RequestBody CreateAlertRequest request) {
        request.setCode("LOGIN_USR_NOT_REGISTERED");
        AlertDTO alert = alertService.createAlert(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(alert);
    }

    @Operation(summary = "Alerta: Intentos excedidos", description = "Registra una alerta cuando un usuario excede el número máximo de intentos de login")
    @ApiResponse(responseCode = "201", description = "Alerta creada exitosamente")
    @PostMapping("/usrexceedattempts")
    public ResponseEntity<AlertDTO> userExceedAttempts(@RequestBody CreateAlertRequest request) {
        request.setCode("LOGIN_USR_ATTEMPS_EXCEEDED");
        AlertDTO alert = alertService.createAlert(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(alert);
    }

    @Operation(summary = "Alerta: Empleado ya ingresó", description = "Registra una alerta cuando se intenta registrar un ingreso duplicado")
    @ApiResponse(responseCode = "201", description = "Alerta creada exitosamente")
    @PostMapping("/employeealreadyentered")
    public ResponseEntity<AlertDTO> employeeAlreadyEntered(@RequestBody CreateAlertRequest request) {
        request.setCode("EMPLOYEE_ALREADY_ENTERED");
        AlertDTO alert = alertService.createAlert(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(alert);
    }

    @Operation(summary = "Alerta: Empleado ya salió", description = "Registra una alerta cuando se intenta registrar una salida duplicada o sin ingreso previo")
    @ApiResponse(responseCode = "201", description = "Alerta creada exitosamente")
    @PostMapping("/employeealreadyleft")
    public ResponseEntity<AlertDTO> employeeAlreadyLeft(@RequestBody CreateAlertRequest request) {
        request.setCode("EMPLOYEE_ALREADY_LEFT");
        AlertDTO alert = alertService.createAlert(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(alert);
    }
}

