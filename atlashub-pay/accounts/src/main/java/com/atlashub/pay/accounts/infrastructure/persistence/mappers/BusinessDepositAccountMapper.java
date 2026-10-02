package com.atlashub.pay.accounts.infrastructure.persistence.mappers;

import com.atlashub.pay.accounts.domain.entities.BusinessDepositAccount;
import com.atlashub.pay.accounts.infrastructure.persistence.entities.BusinessDepositAccountJpa;
import com.atlashub.shared.infrastructure.persistence.mappers.DomainMapper;
import com.atlashub.shared.infrastructure.persistence.mappers.ValueObjectMapper;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR, uses = ValueObjectMapper.class)
public interface BusinessDepositAccountMapper
        extends DomainMapper<BusinessDepositAccount, BusinessDepositAccountJpa> {
    @Override
    @Mapping(target = "accountNumber", ignore = true)
    BusinessDepositAccount toDomain(BusinessDepositAccountJpa jpa);
}
