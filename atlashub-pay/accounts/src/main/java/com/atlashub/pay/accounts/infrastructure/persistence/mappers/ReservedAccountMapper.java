package com.atlashub.pay.accounts.infrastructure.persistence.mappers;

import com.atlashub.pay.accounts.domain.entities.ReservedAccount;
import com.atlashub.pay.accounts.infrastructure.persistence.entities.ReservedAccountJpa;
import com.atlashub.shared.infrastructure.persistence.mappers.DomainMapper;
import com.atlashub.shared.infrastructure.persistence.mappers.ValueObjectMapper;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR, uses = ValueObjectMapper.class)
public interface ReservedAccountMapper extends DomainMapper<ReservedAccount, ReservedAccountJpa> {
    @Override
    @Mapping(target = "accountNumber", ignore = true)
    ReservedAccount toDomain(ReservedAccountJpa jpa);
}
