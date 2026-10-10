package com.atlashub.commerce.storefront.infrastructure.persistence.mappers;

import com.atlashub.commerce.storefront.domain.entities.CustomerCredit;
import com.atlashub.commerce.storefront.infrastructure.persistence.entities.CustomerCreditJpa;
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
public interface CustomerCreditMapper extends DomainMapper<CustomerCredit, CustomerCreditJpa> {

    @Override
    @Mapping(target = "version", ignore = true)
    CustomerCreditJpa toPersistence(CustomerCredit domain);

    @Override
    @Mapping(target = "tle", ignore = true)
    CustomerCredit toDomain(CustomerCreditJpa record);
}
