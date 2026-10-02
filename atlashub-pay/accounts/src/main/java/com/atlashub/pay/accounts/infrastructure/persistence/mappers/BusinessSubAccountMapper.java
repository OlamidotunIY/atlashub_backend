package com.atlashub.pay.accounts.infrastructure.persistence.mappers;

import com.atlashub.pay.accounts.domain.entities.BusinessSubAccount;
import com.atlashub.pay.accounts.infrastructure.persistence.entities.BusinessSubAccountJpa;
import com.atlashub.shared.infrastructure.persistence.mappers.DomainMapper;
import com.atlashub.shared.infrastructure.persistence.mappers.ValueObjectMapper;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR, uses = ValueObjectMapper.class)
public interface BusinessSubAccountMapper extends DomainMapper<BusinessSubAccount, BusinessSubAccountJpa> {
    @Override
    @Mapping(target = "accountNumber", ignore = true)
    BusinessSubAccount toDomain(BusinessSubAccountJpa jpa);
}
