package com.accesscontrol.accesscontrol.domain.port.out;

import java.util.Map;

/**
 * Puerto de salida - Employee Service
 * Arquitectura Hexagonal: Define el contrato para servicio externo
 */
public interface EmployeeServicePort {
    Map<String, Object> getEmployeeByDocument(String document);
    Map<String, Object> getEmployeeByCode(String employeeCode);
}



