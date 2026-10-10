package com.atlashub.commerce.storefront.infrastructure.persistence.mappers;

import com.atlashub.commerce.storefront.domain.entities.Till;
import com.atlashub.commerce.storefront.infrastructure.persistence.entities.TillJpa;
import com.atlashub.shared.infrastructure.persistence.mappers.DomainMapper;
import com.atlashub.shared.infrastructure.persistence.mappers.ValueObjectMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.ERROR,
        uses = {ValueObjectMapper.class}
)
public interface TillMapper extends DomainMapper<Till, TillJpa> {

    @Override
    @Mapping(target = "version", ignore = true)
    TillJpa toPersistence(Till domain);
}
