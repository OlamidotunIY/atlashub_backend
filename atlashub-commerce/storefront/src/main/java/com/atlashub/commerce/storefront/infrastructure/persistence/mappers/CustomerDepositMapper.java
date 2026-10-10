package com.atlashub.commerce.storefront.infrastructure.persistence.mappers;

import com.atlashub.commerce.storefront.domain.entities.CustomerDeposit;
import com.atlashub.commerce.storefront.infrastructure.persistence.entities.CustomerDepositJpa;
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
public interface CustomerDepositMapper extends DomainMapper<CustomerDeposit, CustomerDepositJpa> {

    @Override
    @Mapping(target = "version", ignore = true)
    CustomerDepositJpa toPersistence(CustomerDeposit domain);
}
