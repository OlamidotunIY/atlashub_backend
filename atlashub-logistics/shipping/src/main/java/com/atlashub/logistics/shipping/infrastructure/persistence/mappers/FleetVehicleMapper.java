package com.atlashub.logistics.shipping.infrastructure.persistence.mappers;

import com.atlashub.logistics.shipping.domain.entities.FleetVehicle;
import com.atlashub.logistics.shipping.infrastructure.persistence.entities.FleetVehicleJpa;
import com.atlashub.shared.infrastructure.persistence.mappers.DomainMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface FleetVehicleMapper extends DomainMapper<FleetVehicle, FleetVehicleJpa> {

    @Override
    @Mapping(target = "version", ignore = true)
    FleetVehicleJpa toPersistence(FleetVehicle domain);

    @Override
    FleetVehicle toDomain(FleetVehicleJpa jpa);
}
