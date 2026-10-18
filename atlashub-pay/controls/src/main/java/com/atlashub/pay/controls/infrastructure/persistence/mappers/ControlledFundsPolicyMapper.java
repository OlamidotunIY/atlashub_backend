package com.atlashub.pay.controls.infrastructure.persistence.mappers;

import com.atlashub.pay.controls.domain.entities.ControlledFundsPolicy;
import com.atlashub.pay.controls.infrastructure.persistence.entities.ControlledFundsPolicyJpa;
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
public interface ControlledFundsPolicyMapper extends DomainMapper<ControlledFundsPolicy, ControlledFundsPolicyJpa> {

    @Override
    @Mapping(target = "version", ignore = true)
    ControlledFundsPolicyJpa toPersistence(ControlledFundsPolicy domain);

    @Override
    ControlledFundsPolicy toDomain(ControlledFundsPolicyJpa jpa);
}
