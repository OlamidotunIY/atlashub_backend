package com.atlashub.accounting.gl.infrastructure.persistence.mappers;

import com.atlashub.accounting.gl.domain.entities.LedgerAccountMapping;
import com.atlashub.accounting.gl.infrastructure.persistence.entities.LedgerAccountMappingJpa;
import com.atlashub.shared.infrastructure.persistence.mappers.DomainMapper;
import com.atlashub.shared.infrastructure.persistence.mappers.ValueObjectMapper;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.ERROR,
        uses = {ValueObjectMapper.class}
)
public interface LedgerAccountMappingMapper extends DomainMapper<LedgerAccountMapping, LedgerAccountMappingJpa> {
}
