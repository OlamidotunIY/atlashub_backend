package com.atlashub.logistics.shipping.infrastructure.persistence.mappers;

import com.atlashub.logistics.shipping.domain.entities.ShipmentMilestoneLog;
import com.atlashub.logistics.shipping.infrastructure.persistence.entities.ShipmentMilestoneLogJpa;
import com.atlashub.shared.infrastructure.persistence.mappers.DomainMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface ShipmentMilestoneLogMapper extends DomainMapper<ShipmentMilestoneLog, ShipmentMilestoneLogJpa> {

    @Override
    @Mapping(target = "version", ignore = true)
    ShipmentMilestoneLogJpa toPersistence(ShipmentMilestoneLog domain);

    @Override
    ShipmentMilestoneLog toDomain(ShipmentMilestoneLogJpa jpa);
}
