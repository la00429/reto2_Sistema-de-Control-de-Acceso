package com.accesscontrol.saga.orchestrator;

import com.accesscontrol.saga.model.SagaExecution;
import com.accesscontrol.saga.model.SagaStepExecution;
import com.accesscontrol.saga.service.SagaExecutionService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * SAGA Orchestrator mejorado para registro de acceso
 * Implementa patrón SAGA para transacciones distribuidas con persistencia de estado
 */
@Component
public class ImprovedAccessRegistrationSaga {
    
    @Autowired
    private RabbitTemplate rabbitTemplate;
    
    @Autowired
    private SagaExecutionService sagaExecutionService;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    public static final String SAGA_EXCHANGE = "saga.exchange";
    public static final String EMPLOYEE_SERVICE_QUEUE = "employee.service.queue";
    public static final String ACCESS_CONTROL_SERVICE_QUEUE = "access.control.service.queue";
    
    private static final String ROUTING_KEY_EMPLOYEE_VALIDATE = "employee.validate";
    private static final String ROUTING_KEY_ACCESS_REGISTER = "access.register";
    private static final String ROUTING_KEY_ACCESS_COMPENSATE = "access.compensate";
    
    public SagaExecution executeAccessRegistration(String employeeDocument, String accessType, 
                                                   String location, String deviceId) {
        // Crear payload para la saga
        Map<String, Object> payload = new HashMap<>();
        payload.put("employeeDocument", employeeDocument);
        payload.put("accessType", accessType);
        payload.put("location", location);
        payload.put("deviceId", deviceId);
        
        // Crear ejecución de saga
        SagaExecution saga = sagaExecutionService.createSaga("ACCESS_REGISTRATION", payload);
        sagaExecutionService.markSagaAsInProgress(saga.getSagaId());
        
        try {
            // Paso 1: Validar que el empleado existe
            SagaStepExecution step1 = createStep(saga.getSagaId(), "VALIDATE_EMPLOYEE", 
                "employee-service", null);
            step1.setStatus(SagaStepExecution.StepStatus.IN_PROGRESS);
            
            // Enviar comando a Employee Service
            Map<String, Object> validateCommand = new HashMap<>();
            validateCommand.put("sagaId", saga.getSagaId());
            validateCommand.put("stepId", step1.getId());
            validateCommand.put("action", "validate");
            validateCommand.put("document", employeeDocument);
            
            try {
                String message = objectMapper.writeValueAsString(validateCommand);
                rabbitTemplate.convertAndSend(SAGA_EXCHANGE, ROUTING_KEY_EMPLOYEE_VALIDATE, message);
                step1.setRequestPayload(message);
                
                // En producción, esperaríamos respuesta asíncrona
                // Por ahora simulamos éxito después de enviar
                step1.setStatus(SagaStepExecution.StepStatus.COMPLETED);
                step1.setResponsePayload("{\"valid\": true}");
                step1.setCompletedAt(LocalDateTime.now());
                if (step1.getStartedAt() != null) {
                    step1.setDurationMillis(
                        java.time.Duration.between(step1.getStartedAt(), step1.getCompletedAt()).toMillis());
                }
                sagaExecutionService.addStep(saga.getSagaId(), step1);
                
            } catch (JsonProcessingException e) {
                step1.setStatus(SagaStepExecution.StepStatus.FAILED);
                step1.setErrorMessage(e.getMessage());
                sagaExecutionService.addStep(saga.getSagaId(), step1);
                sagaExecutionService.markSagaAsFailed(saga.getSagaId(), 
                    "Error validating employee: " + e.getMessage());
                return saga;
            }
            
            // Paso 2: Registrar acceso
            SagaStepExecution step2 = createStep(saga.getSagaId(), "REGISTER_ACCESS", 
                "access-control-service", "rollback_access_registration");
            step2.setStatus(SagaStepExecution.StepStatus.IN_PROGRESS);
            
            Map<String, Object> registerCommand = new HashMap<>();
            registerCommand.put("sagaId", saga.getSagaId());
            registerCommand.put("stepId", step2.getId());
            registerCommand.put("action", "register");
            registerCommand.put("employeeDocument", employeeDocument);
            registerCommand.put("accessType", accessType);
            registerCommand.put("location", location);
            registerCommand.put("deviceId", deviceId);
            
            try {
                String message = objectMapper.writeValueAsString(registerCommand);
                rabbitTemplate.convertAndSend(SAGA_EXCHANGE, ROUTING_KEY_ACCESS_REGISTER, message);
                step2.setRequestPayload(message);
                
                // En producción, esperaríamos respuesta asíncrona
                step2.setStatus(SagaStepExecution.StepStatus.COMPLETED);
                step2.setResponsePayload("{\"registered\": true}");
                step2.setCompletedAt(LocalDateTime.now());
                if (step2.getStartedAt() != null) {
                    step2.setDurationMillis(
                        java.time.Duration.between(step2.getStartedAt(), step2.getCompletedAt()).toMillis());
                }
                sagaExecutionService.addStep(saga.getSagaId(), step2);
                
                // Marcar saga como completada
                sagaExecutionService.markSagaAsCompleted(saga.getSagaId());
                
            } catch (JsonProcessingException e) {
                step2.setStatus(SagaStepExecution.StepStatus.FAILED);
                step2.setErrorMessage(e.getMessage());
                sagaExecutionService.addStep(saga.getSagaId(), step2);
                
                // Compensar saga
                compensateSaga(saga.getSagaId());
                return saga;
            }
            
        } catch (Exception e) {
            sagaExecutionService.markSagaAsFailed(saga.getSagaId(), 
                "Unexpected error: " + e.getMessage());
            compensateSaga(saga.getSagaId());
            return saga;
        }
        
        return sagaExecutionService.getSagaBySagaId(saga.getSagaId()).orElse(saga);
    }
    
    private SagaStepExecution createStep(String sagaId, String stepName, 
                                        String serviceTarget, String compensationAction) {
        SagaStepExecution step = new SagaStepExecution();
        step.setStepName(stepName);
        step.setServiceTarget(serviceTarget);
        step.setCompensationAction(compensationAction);
        step.setStatus(SagaStepExecution.StepStatus.PENDING);
        step.setStartedAt(LocalDateTime.now());
        return step;
    }
    
    private void compensateSaga(String sagaId) {
        sagaExecutionService.markSagaAsCompensating(sagaId);
        
        // Obtener todos los pasos de la saga
        SagaExecution saga = sagaExecutionService.getSagaBySagaId(sagaId).orElse(null);
        if (saga == null) {
            return;
        }
        
        List<SagaStepExecution> steps = saga.getSteps();
        
        // Ejecutar compensaciones en orden inverso
        for (int i = steps.size() - 1; i >= 0; i--) {
            SagaStepExecution step = steps.get(i);
            if (step.getStatus() == SagaStepExecution.StepStatus.COMPLETED 
                && step.getCompensationAction() != null) {
                
                executeCompensation(step);
            }
        }
        
        sagaExecutionService.markSagaAsCompensated(sagaId);
    }
    
    private void executeCompensation(SagaStepExecution step) {
        if (step.getStepName().equals("REGISTER_ACCESS")) {
            try {
                Map<String, Object> compensateCommand = new HashMap<>();
                compensateCommand.put("sagaId", step.getSagaExecution().getSagaId());
                compensateCommand.put("stepId", step.getId());
                compensateCommand.put("action", "compensate");
                compensateCommand.put("compensationAction", step.getCompensationAction());
                
                String message = objectMapper.writeValueAsString(compensateCommand);
                rabbitTemplate.convertAndSend(SAGA_EXCHANGE, ROUTING_KEY_ACCESS_COMPENSATE, message);
                
                step.setStatus(SagaStepExecution.StepStatus.COMPENSATED);
                step.setCompletedAt(LocalDateTime.now());
                
            } catch (JsonProcessingException e) {
                System.err.println("Error executing compensation: " + e.getMessage());
            }
        }
    }
}

