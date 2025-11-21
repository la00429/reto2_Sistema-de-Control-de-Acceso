package com.accesscontrol.accesscontrol.infrastructure.adapter.out.persistence.mapper;

import com.accesscontrol.accesscontrol.domain.model.AccessRecord;
import org.springframework.stereotype.Component;

/**
 * Mapper entre entidad de dominio y entidad JPA
 * Arquitectura Hexagonal: Infrastructure Layer
 */
@Component
public class AccessRecordMapper {
    
    public com.accesscontrol.accesscontrol.model.AccessRecord toJpaEntity(AccessRecord domain) {
        com.accesscontrol.accesscontrol.model.AccessRecord jpa = new com.accesscontrol.accesscontrol.model.AccessRecord();
        jpa.setId(domain.getId());
        jpa.setEmployeeID(domain.getEmployeeID());
        jpa.setAccessdatetime(domain.getAccessdatetime());
        jpa.setAccessType(convertAccessType(domain.getAccessType()));
        jpa.setAccessTimestamp(domain.getAccessTimestamp());
        jpa.setLocation(domain.getLocation());
        jpa.setDeviceId(domain.getDeviceId());
        jpa.setStatus(convertAccessStatus(domain.getStatus()));
        jpa.setNotes(domain.getNotes());
        return jpa;
    }
    
    public AccessRecord toDomainEntity(com.accesscontrol.accesscontrol.model.AccessRecord jpa) {
        AccessRecord domain = new AccessRecord();
        domain.setId(jpa.getId());
        domain.setEmployeeID(jpa.getEmployeeID());
        domain.setAccessdatetime(jpa.getAccessdatetime());
        domain.setAccessType(convertAccessTypeFromJpa(jpa.getAccessType()));
        domain.setAccessTimestamp(jpa.getAccessTimestamp());
        domain.setLocation(jpa.getLocation());
        domain.setDeviceId(jpa.getDeviceId());
        domain.setStatus(convertAccessStatusFromJpa(jpa.getStatus()));
        domain.setNotes(jpa.getNotes());
        return domain;
    }
    
    private com.accesscontrol.accesscontrol.model.AccessRecord.AccessType convertAccessType(
            AccessRecord.AccessType domainType) {
        if (domainType == null) return null;
        return com.accesscontrol.accesscontrol.model.AccessRecord.AccessType.valueOf(domainType.name());
    }
    
    private AccessRecord.AccessType convertAccessTypeFromJpa(
            com.accesscontrol.accesscontrol.model.AccessRecord.AccessType jpaType) {
        if (jpaType == null) return null;
        return AccessRecord.AccessType.valueOf(jpaType.name());
    }
    
    private com.accesscontrol.accesscontrol.model.AccessRecord.AccessStatus convertAccessStatus(
            AccessRecord.AccessStatus domainStatus) {
        if (domainStatus == null) return null;
        return com.accesscontrol.accesscontrol.model.AccessRecord.AccessStatus.valueOf(domainStatus.name());
    }
    
    private AccessRecord.AccessStatus convertAccessStatusFromJpa(
            com.accesscontrol.accesscontrol.model.AccessRecord.AccessStatus jpaStatus) {
        if (jpaStatus == null) return null;
        return AccessRecord.AccessStatus.valueOf(jpaStatus.name());
    }
}
