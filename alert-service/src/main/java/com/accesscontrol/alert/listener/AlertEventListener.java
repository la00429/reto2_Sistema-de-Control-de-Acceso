package com.accesscontrol.alert.listener;

import com.accesscontrol.alert.dto.CreateAlertRequest;
import com.accesscontrol.alert.service.AlertService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class AlertEventListener {
    
    @Autowired
    private AlertService alertService;

    @RabbitListener(queues = "alert.queue")
    public void handleAlertEvent(Object event) {
        CreateAlertRequest request = new CreateAlertRequest();
        
        // Manejar diferentes tipos de eventos
        if (event instanceof Map) {
            Map<String, Object> eventMap = (Map<String, Object>) event;
            request.setCode((String) eventMap.get("code"));
            request.setDescription((String) eventMap.get("description"));
            request.setUsername((String) eventMap.get("username"));
            request.setEmployeeCode((String) eventMap.get("employeeCode"));
            request.setIpAddress((String) eventMap.get("ipAddress"));
        } else if (event instanceof CreateAlertRequest) {
            request = (CreateAlertRequest) event;
        } else {
            // Intentar convertir usando reflexión
            try {
                java.lang.reflect.Method getCode = event.getClass().getMethod("getCode");
                java.lang.reflect.Method getDescription = event.getClass().getMethod("getDescription");
                java.lang.reflect.Method getEmployeeCode = event.getClass().getMethod("getEmployeeCode");
                
                request.setCode((String) getCode.invoke(event));
                request.setDescription((String) getDescription.invoke(event));
                request.setEmployeeCode((String) getEmployeeCode.invoke(event));
            } catch (Exception e) {
                System.err.println("Error procesando evento de alerta: " + e.getMessage());
                return;
            }
        }
        
        alertService.createAlert(request);
    }
}

