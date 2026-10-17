package com.atlashub.logistics.warehousing.infrastructure.persistence.mappers;

import com.atlashub.logistics.warehousing.domain.entities.WarehouseInventory;
import com.atlashub.logistics.warehousing.infrastructure.persistence.entities.WarehouseInventoryJpa;
import com.atlashub.shared.infrastructure.persistence.mappers.DomainMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface WarehouseInventoryMapper extends DomainMapper<WarehouseInventory, WarehouseInventoryJpa> {

    @Override
    @Mapping(target = "version", ignore = true)
    WarehouseInventoryJpa toPersistence(WarehouseInventory domain);

    @Override
    default WarehouseInventory toDomain(WarehouseInventoryJpa jpa) {
        if (jpa == null) {
            return null;
        }
        return new WarehouseInventory(
                jpa.getId(),
                jpa.getOrganizationId(),
                jpa.getWarehouseId(),
                jpa.getProductId(),
                jpa.getQuantity(),
                jpa.getReservedQuantity(),
                jpa.getUpdatedAt()
        );
    }
}
