package com.atlashub.pay.accounts.infrastructure.persistence.mappers;

import com.atlashub.pay.accounts.domain.entities.BankingProviderRequest;
import com.atlashub.pay.accounts.infrastructure.persistence.entities.BankingProviderRequestJpa;
import com.atlashub.shared.infrastructure.persistence.mappers.DomainMapper;
import com.atlashub.shared.infrastructure.persistence.mappers.ValueObjectMapper;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR, uses = ValueObjectMapper.class)
public interface BankingProviderRequestMapper
        extends DomainMapper<BankingProviderRequest, BankingProviderRequestJpa> {
}
