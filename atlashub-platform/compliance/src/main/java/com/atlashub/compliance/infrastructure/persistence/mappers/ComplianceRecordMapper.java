package com.atlashub.compliance.infrastructure.persistence.mappers;

import com.atlashub.compliance.domain.entities.ComplianceRecord;
import com.atlashub.compliance.infrastructure.persistence.entities.ComplianceRecordJpa;
import com.atlashub.shared.infrastructure.persistence.mappers.DomainMapper;
import com.atlashub.shared.infrastructure.persistence.mappers.ValueObjectMapper;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.ERROR,
        uses = {ValueObjectMapper.class}
)
public interface ComplianceRecordMapper extends DomainMapper<ComplianceRecord, ComplianceRecordJpa> {
}
