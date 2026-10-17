package com.atlashub.logistics.warehousing.infrastructure.persistence.mappers;

import com.atlashub.logistics.warehousing.domain.entities.Warehouse;
import com.atlashub.logistics.warehousing.domain.valueobject.Address;
import com.atlashub.logistics.warehousing.infrastructure.persistence.entities.WarehouseJpa;
import com.atlashub.shared.infrastructure.persistence.mappers.DomainMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface WarehouseMapper extends DomainMapper<Warehouse, WarehouseJpa> {

    @Override
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "street", source = "address.street")
    @Mapping(target = "city", source = "address.city")
    @Mapping(target = "state", source = "address.state")
    @Mapping(target = "country", source = "address.country")
    @Mapping(target = "postalCode", source = "address.postalCode")
    WarehouseJpa toPersistence(Warehouse domain);

    @Override
    default Warehouse toDomain(WarehouseJpa jpa) {
        if (jpa == null) {
            return null;
        }
        Address address = new Address(
                jpa.getStreet(),
                jpa.getCity(),
                jpa.getState(),
                jpa.getCountry(),
                jpa.getPostalCode()
        );
        return new Warehouse(
                jpa.getId(),
                jpa.getOrganizationId(),
                jpa.getName(),
                jpa.getCode(),
                jpa.getFacilityType(),
                address,
                jpa.getManagerId(),
                jpa.getStatus(),
                jpa.getCreatedAt()
        );
    }
}
