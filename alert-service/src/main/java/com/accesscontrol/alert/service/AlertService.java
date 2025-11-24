package com.accesscontrol.alert.service;

import com.accesscontrol.alert.dto.AlertDTO;
import com.accesscontrol.alert.dto.CreateAlertRequest;
import com.accesscontrol.alert.model.Alert;
import com.accesscontrol.alert.repository.AlertRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class AlertService {
    
    @Autowired
    private AlertRepository alertRepository;

    public AlertDTO createAlert(CreateAlertRequest request) {
        Alert alert = new Alert();
        alert.setCode(request.getCode());
        alert.setDescription(request.getDescription());
        alert.setUsername(request.getUsername());
        alert.setEmployeeCode(request.getEmployeeCode());
        alert.setIpAddress(request.getIpAddress());
        alert.setTimestamp(LocalDateTime.now());
        
        Alert saved = alertRepository.save(alert);
        return new AlertDTO(saved);
    }

    public List<AlertDTO> getAllAlerts() {
        return alertRepository.findAll().stream()
                .map(AlertDTO::new)
                .collect(Collectors.toList());
    }

    public List<AlertDTO> getAlertsByCode(String code) {
        return alertRepository.findByCode(code).stream()
                .map(AlertDTO::new)
                .collect(Collectors.toList());
    }

    public List<AlertDTO> getAlertsByUsername(String username) {
        return alertRepository.findByUsername(username).stream()
                .map(AlertDTO::new)
                .collect(Collectors.toList());
    }

    public List<AlertDTO> getRecentAlertsByUserAndCode(String username, String code, int minutes) {
        LocalDateTime since = LocalDateTime.now().minusMinutes(minutes);
        return alertRepository.findRecentAlertsByUserAndCode(username, code, since).stream()
                .map(AlertDTO::new)
                .collect(Collectors.toList());
    }
}





