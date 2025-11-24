package com.accesscontrol.accesscontrol.domain.port.in;

import com.accesscontrol.accesscontrol.domain.model.AccessRecord;

/**
 * Puerto de entrada - Caso de uso
 * Arquitectura Hexagonal: Define el contrato para casos de uso
 */
public interface RegisterAccessUseCasePort {
    AccessRecord execute(AccessRecord accessRecord);
}





