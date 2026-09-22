package com.atlashub.accounts.infrastructure.persistence.mappers;

import com.atlashub.accounts.domain.model.Organization;
import com.atlashub.accounts.infrastructure.persistence.entities.OrganizationJPA;
import com.atlashub.shared.infrastructure.persistence.mappers.DomainMapper;
import com.atlashub.shared.infrastructure.persistence.mappers.ValueObjectMapper;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.ERROR,
        uses = {ValueObjectMapper.class}
)
public interface OrganizationMapper extends DomainMapper<Organization, OrganizationJPA> {
}
