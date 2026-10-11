package com.atlashub.audit.infrastructure.persistence.mappers;

import com.atlashub.audit.domain.entities.AuditLog;
import com.atlashub.audit.infrastructure.persistence.entities.AuditLogJpa;
import com.atlashub.shared.infrastructure.persistence.mappers.DomainMapper;
import com.atlashub.shared.infrastructure.persistence.mappers.ValueObjectMapper;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.ERROR,
        uses = {ValueObjectMapper.class}
)
public interface AuditLogMapper extends DomainMapper<AuditLog, AuditLogJpa> {
}
