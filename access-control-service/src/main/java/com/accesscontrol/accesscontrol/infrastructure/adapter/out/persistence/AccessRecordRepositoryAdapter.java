package com.accesscontrol.accesscontrol.infrastructure.adapter.out.persistence;

import com.accesscontrol.accesscontrol.domain.model.AccessRecord;
import com.accesscontrol.accesscontrol.domain.port.out.AccessRecordRepositoryPort;
import com.accesscontrol.accesscontrol.infrastructure.adapter.out.persistence.mapper.AccessRecordMapper;
import com.accesscontrol.accesscontrol.infrastructure.adapter.out.persistence.jpa.AccessRecordJpaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Adaptador de persistencia
 * Arquitectura Hexagonal: Infrastructure Layer - Adapta JPA a puerto de dominio
 */
@Component
public class AccessRecordRepositoryAdapter implements AccessRecordRepositoryPort {
    
    @Autowired
    private AccessRecordJpaRepository jpaRepository;
    
    @Autowired
    private AccessRecordMapper mapper;

    @Override
    public AccessRecord save(AccessRecord accessRecord) {
        var jpaEntity = mapper.toJpaEntity(accessRecord);
        var saved = jpaRepository.save(jpaEntity);
        return mapper.toDomainEntity(saved);
    }

    @Override
    public Optional<AccessRecord> findById(Long id) {
        return jpaRepository.findById(id)
                .map(mapper::toDomainEntity);
    }

    @Override
    public List<AccessRecord> findByEmployeeID(String employeeID) {
        return jpaRepository.findByEmployeeID(employeeID).stream()
                .map(mapper::toDomainEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<AccessRecord> findByEmployeeCode(String employeeCode) {
        return jpaRepository.findByEmployeeCode(employeeCode).stream()
                .map(mapper::toDomainEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<AccessRecord> findByAccessTimestampBetween(LocalDateTime start, LocalDateTime end) {
        return jpaRepository.findByAccessTimestampBetween(start, end).stream()
                .map(mapper::toDomainEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<AccessRecord> findByEmployeeIDAndDateRange(String employeeID, LocalDateTime start, LocalDateTime end) {
        return jpaRepository.findByEmployeeIDAndDateRange(employeeID, start, end).stream()
                .map(mapper::toDomainEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<AccessRecord> findByEmployeeCodeAndDateRange(String employeeCode, LocalDateTime start, LocalDateTime end) {
        return jpaRepository.findByEmployeeCodeAndDateRange(employeeCode, start, end).stream()
                .map(mapper::toDomainEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<AccessRecord> findLatestByEmployeeID(String employeeID) {
        return jpaRepository.findLatestByEmployeeID(employeeID).stream()
                .map(mapper::toDomainEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<AccessRecord> findLatestByEmployeeCode(String employeeCode) {
        return jpaRepository.findLatestByEmployeeCode(employeeCode).stream()
                .map(mapper::toDomainEntity)
                .collect(Collectors.toList());
    }
}



