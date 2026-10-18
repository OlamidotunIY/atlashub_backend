package com.atlashub.logistics.shipping.infrastructure.persistence.mappers;

import com.atlashub.logistics.shipping.domain.entities.DispatchRider;
import com.atlashub.logistics.shipping.infrastructure.persistence.entities.DispatchRiderJpa;
import com.atlashub.shared.infrastructure.persistence.mappers.DomainMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface DispatchRiderMapper extends DomainMapper<DispatchRider, DispatchRiderJpa> {

    @Override
    @Mapping(target = "version", ignore = true)
    DispatchRiderJpa toPersistence(DispatchRider domain);

    @Override
    DispatchRider toDomain(DispatchRiderJpa jpa);
}
