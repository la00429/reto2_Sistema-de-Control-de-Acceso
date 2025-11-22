package com.accesscontrol.saga.orchestrator;

import com.accesscontrol.saga.model.SagaStep;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * SAGA Orchestrator para registro de acceso
 * Implementa patrón SAGA para transacciones distribuidas
 */
@Component
public class AccessRegistrationSaga {
    
    @Autowired
    private RabbitTemplate rabbitTemplate;
    
    private static final String EMPLOYEE_SERVICE_QUEUE = "employee.service.queue";
    private static final String ACCESS_CONTROL_SERVICE_QUEUE = "access.control.service.queue";
    
    public String executeAccessRegistration(String employeeDocument, String accessType) {
        String sagaId = UUID.randomUUID().toString();
        List<SagaStep> steps = new ArrayList<>();
        
        try {
            // Paso 1: Validar que el empleado existe
            SagaStep step1 = validateEmployee(employeeDocument, sagaId);
            steps.add(step1);
            
            if (!step1.isSuccess()) {
                return compensate(sagaId, steps);
            }
            
            // Paso 2: Registrar acceso
            SagaStep step2 = registerAccess(employeeDocument, accessType, sagaId);
            steps.add(step2);
            
            if (!step2.isSuccess()) {
                return compensate(sagaId, steps);
            }
            
            return "SAGA completed successfully: " + sagaId;
            
        } catch (Exception e) {
            return compensate(sagaId, steps);
        }
    }
    
    private SagaStep validateEmployee(String employeeDocument, String sagaId) {
        try {
            // Enviar comando a Employee Service
            String message = String.format("{\"sagaId\":\"%s\",\"action\":\"validate\",\"document\":\"%s\"}", 
                sagaId, employeeDocument);
            rabbitTemplate.convertAndSend(EMPLOYEE_SERVICE_QUEUE, message);
            
            // En una implementación real, esperaríamos la respuesta
            // Por ahora simulamos éxito
            SagaStep step = new SagaStep();
            step.setSagaId(sagaId);
            step.setStepName("VALIDATE_EMPLOYEE");
            step.setSuccess(true);
            step.setCompensationAction("rollback_employee_validation");
            return step;
        } catch (Exception e) {
            SagaStep step = new SagaStep();
            step.setSagaId(sagaId);
            step.setStepName("VALIDATE_EMPLOYEE");
            step.setSuccess(false);
            step.setErrorMessage(e.getMessage());
            return step;
        }
    }
    
    private SagaStep registerAccess(String employeeDocument, String accessType, String sagaId) {
        try {
            // Enviar comando a Access Control Service
            String message = String.format("{\"sagaId\":\"%s\",\"action\":\"register\",\"employeeDocument\":\"%s\",\"accessType\":\"%s\"}", 
                sagaId, employeeDocument, accessType);
            rabbitTemplate.convertAndSend(ACCESS_CONTROL_SERVICE_QUEUE, message);
            
            SagaStep step = new SagaStep();
            step.setSagaId(sagaId);
            step.setStepName("REGISTER_ACCESS");
            step.setSuccess(true);
            step.setCompensationAction("rollback_access_registration");
            return step;
        } catch (Exception e) {
            SagaStep step = new SagaStep();
            step.setSagaId(sagaId);
            step.setStepName("REGISTER_ACCESS");
            step.setSuccess(false);
            step.setErrorMessage(e.getMessage());
            return step;
        }
    }
    
    private String compensate(String sagaId, List<SagaStep> steps) {
        // Ejecutar compensaciones en orden inverso
        for (int i = steps.size() - 1; i >= 0; i--) {
            SagaStep step = steps.get(i);
            if (step.isSuccess() && step.getCompensationAction() != null) {
                executeCompensation(step);
            }
        }
        return "SAGA compensated: " + sagaId;
    }
    
    private void executeCompensation(SagaStep step) {
        // Ejecutar acción de compensación
        String compensationMessage = String.format("{\"sagaId\":\"%s\",\"action\":\"%s\"}", 
            step.getSagaId(), step.getCompensationAction());
        
        if (step.getStepName().equals("VALIDATE_EMPLOYEE")) {
            // No hay compensación necesaria para validación
        } else if (step.getStepName().equals("REGISTER_ACCESS")) {
            rabbitTemplate.convertAndSend(ACCESS_CONTROL_SERVICE_QUEUE, compensationMessage);
        }
    }
}



