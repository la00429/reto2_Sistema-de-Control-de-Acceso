package com.accesscontrol.accesscontrol.application.usecase;

import com.accesscontrol.accesscontrol.domain.model.AccessRecord;
import com.accesscontrol.accesscontrol.domain.port.in.RegisterAccessUseCasePort;
import com.accesscontrol.accesscontrol.domain.port.out.AccessRecordRepositoryPort;
import com.accesscontrol.accesscontrol.domain.port.out.AlertServicePort;
import com.accesscontrol.accesscontrol.domain.service.AccessValidationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Caso de uso: Registrar acceso
 * Arquitectura Hexagonal: Application Layer
 */
@Service
@Transactional
public class RegisterAccessUseCase implements RegisterAccessUseCasePort {
    
    private final AccessRecordRepositoryPort repository;
    private final AlertServicePort alertService;
    private final AccessValidationService validationService;

    public RegisterAccessUseCase(
            AccessRecordRepositoryPort repository,
            AlertServicePort alertService,
            AccessValidationService validationService) {
        this.repository = repository;
        this.alertService = alertService;
        this.validationService = validationService;
    }

    @Override
    public AccessRecord execute(AccessRecord accessRecord) {
        // Validar acceso usando servicio de dominio
        validationService.validateAccess(accessRecord, repository, alertService);
        
        // Guardar registro
        return repository.save(accessRecord);
    }
}



