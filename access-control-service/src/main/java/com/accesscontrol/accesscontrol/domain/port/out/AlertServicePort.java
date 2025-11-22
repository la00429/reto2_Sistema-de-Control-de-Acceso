package com.accesscontrol.accesscontrol.domain.port.out;

/**
 * Puerto de salida - Alert Service
 * Arquitectura Hexagonal: Define el contrato para envío de alertas
 */
public interface AlertServicePort {
    void sendAlert(String code, String description, String employeeIdentifier);
}




