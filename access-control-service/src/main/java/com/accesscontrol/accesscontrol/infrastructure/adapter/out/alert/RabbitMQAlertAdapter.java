package com.accesscontrol.accesscontrol.infrastructure.adapter.out.alert;

import com.accesscontrol.accesscontrol.domain.port.out.AlertServicePort;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Adaptador de alertas - RabbitMQ
 * Arquitectura Hexagonal: Infrastructure Layer
 */
@Component
public class RabbitMQAlertAdapter implements AlertServicePort {
    
    @Autowired
    private RabbitTemplate rabbitTemplate;
    
    private static final String ALERT_QUEUE = "alert.queue";

    @Override
    public void sendAlert(String code, String description, String employeeIdentifier) {
        try {
            Map<String, Object> alertEvent = new HashMap<>();
            alertEvent.put("code", code);
            alertEvent.put("description", description);
            alertEvent.put("employeeCode", employeeIdentifier);
            alertEvent.put("timestamp", LocalDateTime.now().toString());
            
            rabbitTemplate.convertAndSend(ALERT_QUEUE, alertEvent);
        } catch (Exception e) {
            System.err.println("Error enviando alerta: " + e.getMessage());
        }
    }
}



