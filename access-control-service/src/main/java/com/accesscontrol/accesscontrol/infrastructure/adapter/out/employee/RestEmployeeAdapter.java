package com.accesscontrol.accesscontrol.infrastructure.adapter.out.employee;

import com.accesscontrol.accesscontrol.domain.port.out.EmployeeServicePort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

/**
 * Adaptador de Employee Service - REST
 * Arquitectura Hexagonal: Infrastructure Layer
 */
@Component
public class RestEmployeeAdapter implements EmployeeServicePort {
    
    private final RestTemplate restTemplate;
    
    @Value("${employee.service.url:http://localhost:8082}")
    private String employeeServiceUrl;
    
    public RestEmployeeAdapter(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }
    
    @Override
    public Map<String, Object> getEmployeeByDocument(String document) {
        try {
            ResponseEntity<Map> response = restTemplate.getForEntity(
                employeeServiceUrl + "/employee/document/" + document,
                Map.class
            );
            return response.getBody();
        } catch (Exception e) {
            return null;
        }
    }
    
    @Override
    public Map<String, Object> getEmployeeByCode(String employeeCode) {
        try {
            ResponseEntity<Map> response = restTemplate.getForEntity(
                employeeServiceUrl + "/employee/code/" + employeeCode,
                Map.class
            );
            return response.getBody();
        } catch (Exception e) {
            return null;
        }
    }
}




