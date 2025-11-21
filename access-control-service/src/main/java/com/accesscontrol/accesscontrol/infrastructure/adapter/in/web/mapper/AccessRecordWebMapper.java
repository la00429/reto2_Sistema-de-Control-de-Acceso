package com.accesscontrol.accesscontrol.infrastructure.adapter.in.web.mapper;

import com.accesscontrol.accesscontrol.domain.model.AccessRecord;
import com.accesscontrol.accesscontrol.dto.AccessRecordDTO;
import org.springframework.stereotype.Component;

/**
 * Mapper entre DTO web y entidad de dominio
 * Arquitectura Hexagonal: Infrastructure Layer
 */
@Component
public class AccessRecordWebMapper {
    
    public AccessRecord toDomainEntity(AccessRecordDTO dto) {
        AccessRecord domain = new AccessRecord();
        domain.setId(dto.getId());
        domain.setEmployeeID(dto.getEmployeeID());
        domain.setAccessdatetime(dto.getAccessdatetime());
        
        if (dto.getAccessType() != null) {
            try {
                domain.setAccessType(AccessRecord.AccessType.valueOf(dto.getAccessType().toUpperCase()));
            } catch (IllegalArgumentException e) {
                throw new RuntimeException("Invalid access type: " + dto.getAccessType());
            }
        }
        
        domain.setAccessTimestamp(dto.getAccessTimestamp());
        domain.setLocation(dto.getLocation());
        domain.setDeviceId(dto.getDeviceId());
        
        if (dto.getStatus() != null) {
            try {
                domain.setStatus(AccessRecord.AccessStatus.valueOf(dto.getStatus().toUpperCase()));
            } catch (IllegalArgumentException e) {
                domain.setStatus(AccessRecord.AccessStatus.SUCCESS);
            }
        } else {
            domain.setStatus(AccessRecord.AccessStatus.SUCCESS);
        }
        
        domain.setNotes(dto.getNotes());
        return domain;
    }
    
    public AccessRecordDTO toDTO(AccessRecord domain) {
        AccessRecordDTO dto = new AccessRecordDTO();
        dto.setId(domain.getId());
        dto.setEmployeeID(domain.getEmployeeID());
        dto.setAccessdatetime(domain.getAccessdatetime());
        dto.setAccessType(domain.getAccessType() != null ? domain.getAccessType().name() : null);
        dto.setAccessTimestamp(domain.getAccessTimestamp());
        dto.setLocation(domain.getLocation());
        dto.setDeviceId(domain.getDeviceId());
        dto.setStatus(domain.getStatus() != null ? domain.getStatus().name() : null);
        dto.setNotes(domain.getNotes());
        return dto;
    }
}

