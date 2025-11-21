package com.accesscontrol.accesscontrol.domain.service;

import com.accesscontrol.accesscontrol.domain.model.AccessRecord;
import com.accesscontrol.accesscontrol.domain.port.out.AccessRecordRepositoryPort;
import com.accesscontrol.accesscontrol.domain.port.out.AlertServicePort;
import com.accesscontrol.accesscontrol.exception.AccessValidationException;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Servicio de dominio - Validación de accesos
 * Arquitectura Hexagonal: Domain Layer - Lógica de negocio
 */
@Service
public class AccessValidationService {
    
    public void validateAccess(AccessRecord newRecord, 
                               AccessRecordRepositoryPort repository,
                               AlertServicePort alertService) {
        String employeeIdentifier = newRecord.getEmployeeID();

        if (employeeIdentifier == null || employeeIdentifier.isBlank()) {
            throw new AccessValidationException(
                "El documento del empleado (employeeID) es obligatorio para registrar el acceso",
                "MISSING_EMPLOYEE_ID"
            );
        }
        AccessRecord.AccessType requestedType = newRecord.getAccessType();
        
        // Obtener el último acceso del empleado
        List<AccessRecord> latestRecords = repository.findLatestByEmployeeID(employeeIdentifier);
        
        if (latestRecords.isEmpty()) {
            // Si no hay registros previos y se intenta salir, es un error
            if (requestedType == AccessRecord.AccessType.EXIT) {
                alertService.sendAlert("EMPLOYEE_ALREADY_LEFT", 
                    "Intento de salida sin ingreso previo para empleado: " + employeeIdentifier, 
                    employeeIdentifier);
                throw new AccessValidationException(
                    "No se puede registrar salida sin un ingreso previo",
                    "EMPLOYEE_ALREADY_LEFT"
                );
            }
            return;
        }
        
        AccessRecord lastRecord = latestRecords.get(0);
        
        if (requestedType == AccessRecord.AccessType.ENTRY) {
            // Validar que no haya un ingreso previo sin salida
            if (lastRecord.isEntry()) {
                alertService.sendAlert("EMPLOYEE_ALREADY_ENTERED", 
                    "Intento de ingreso duplicado para empleado: " + employeeIdentifier + 
                    ". Último ingreso: " + lastRecord.getAccessdatetime(), 
                    employeeIdentifier);
                throw new AccessValidationException(
                    "El empleado ya tiene un ingreso registrado sin salida correspondiente",
                    "EMPLOYEE_ALREADY_ENTERED"
                );
            }
        } else if (requestedType == AccessRecord.AccessType.EXIT) {
            // Validar que haya un ingreso previo
            if (lastRecord.isExit()) {
                alertService.sendAlert("EMPLOYEE_ALREADY_LEFT", 
                    "Intento de salida duplicado para empleado: " + employeeIdentifier + 
                    ". Última salida: " + lastRecord.getAccessdatetime(), 
                    employeeIdentifier);
                throw new AccessValidationException(
                    "El empleado ya tiene una salida registrada sin ingreso correspondiente",
                    "EMPLOYEE_ALREADY_LEFT"
                );
            }
        }
    }
}


