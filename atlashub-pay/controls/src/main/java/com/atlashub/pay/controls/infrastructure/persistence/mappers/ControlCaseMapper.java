package com.atlashub.pay.controls.infrastructure.persistence.mappers;

import com.atlashub.pay.controls.domain.entities.ControlCase;
import com.atlashub.pay.controls.infrastructure.persistence.entities.ControlCaseJpa;
import com.atlashub.shared.infrastructure.persistence.mappers.DomainMapper;
import com.atlashub.shared.infrastructure.persistence.mappers.ValueObjectMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        uses = {ValueObjectMapper.class, ControlsJsonMapper.class}
)
public interface ControlCaseMapper extends DomainMapper<ControlCase, ControlCaseJpa> {

    @Override
    @Mapping(target = "version", ignore = true)
    ControlCaseJpa toPersistence(ControlCase domain);

    @Override
    ControlCase toDomain(ControlCaseJpa jpa);
}
