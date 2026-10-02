package com.atlashub.pay.accounts.infrastructure.persistence.mappers;

import com.atlashub.pay.accounts.domain.entities.OrganizationBankingProfile;
import com.atlashub.pay.accounts.infrastructure.persistence.entities.OrganizationBankingProfileJpa;
import com.atlashub.shared.infrastructure.persistence.mappers.DomainMapper;
import com.atlashub.shared.infrastructure.persistence.mappers.ValueObjectMapper;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR, uses = ValueObjectMapper.class)
public interface OrganizationBankingProfileMapper
        extends DomainMapper<OrganizationBankingProfile, OrganizationBankingProfileJpa> {
}
