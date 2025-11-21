package com.accesscontrol.accesscontrol.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

/**
 * Cliente REST para comunicarse con Employee Service
 */
@Component
public class EmployeeClient {
    
    private final RestTemplate restTemplate;
    
    @Value("${employee.service.url:http://localhost:8082}")
    private String employeeServiceUrl;
    
    public EmployeeClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }
    
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



