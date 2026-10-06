package com.atlashub.accounts.infrastructure.persistence.mappers;

import com.atlashub.accounts.domain.entities.Outlet;
import com.atlashub.accounts.infrastructure.persistence.entities.OutletJpa;
import com.atlashub.shared.infrastructure.persistence.mappers.DomainMapper;
import com.atlashub.shared.infrastructure.persistence.mappers.ValueObjectMapper;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.ERROR,
        uses = {ValueObjectMapper.class}
)
public interface OutletMapper extends DomainMapper<Outlet, OutletJpa> {
}
