package com.atlashub.logistics.warehousing.domain.entities;

import com.atlashub.logistics.warehousing.domain.events.WarehouseCreatedEvent;
import com.atlashub.logistics.warehousing.domain.exceptions.WarehousingInvariantException;
import com.atlashub.logistics.warehousing.domain.valueobject.Address;
import com.atlashub.logistics.warehousing.domain.valueobject.FacilityType;
import com.atlashub.logistics.warehousing.domain.valueobject.WarehouseStatus;
import com.atlashub.shared.domain.entities.AggregateRoot;
import java.time.ZonedDateTime;
import lombok.Getter;

@Getter
public class Warehouse extends AggregateRoot<Long> {

    private final Long id;
    private final Long organizationId;
    private String name;
    private final String code;
    private final FacilityType facilityType;
    private Address address;
    private Long managerId;
    private WarehouseStatus status;
    private final ZonedDateTime createdAt;

    public Warehouse(
            Long id,
            Long organizationId,
            String name,
            String code,
            FacilityType facilityType,
            Address address,
            Long managerId,
            WarehouseStatus status,
            ZonedDateTime createdAt
    ) {
        this.id = id;
        this.organizationId = organizationId;
        this.name = name;
        this.code = code;
        this.facilityType = facilityType;
        this.address = address;
        this.managerId = managerId;
        this.status = status;
        this.createdAt = createdAt;
    }

    public static Warehouse create(
            Long id,
            Long organizationId,
            String name,
            String code,
            FacilityType facilityType,
            Address address,
            Long managerId,
            String correlationId
    ) {
        if (id == null) {
            throw new WarehousingInvariantException("Warehouse ID cannot be null");
        }
        if (organizationId == null) {
            throw new WarehousingInvariantException("Organization ID cannot be null");
        }
        if (name == null || name.isBlank()) {
            throw new WarehousingInvariantException("Warehouse name cannot be blank");
        }
        if (code == null || code.isBlank()) {
            throw new WarehousingInvariantException("Warehouse code cannot be blank");
        }
        if (facilityType == null) {
            throw new WarehousingInvariantException("Facility type cannot be null");
        }
        if (address == null) {
            throw new WarehousingInvariantException("Warehouse address cannot be null");
        }

        ZonedDateTime now = ZonedDateTime.now();
        Warehouse warehouse = new Warehouse(
                id,
                organizationId,
                name.trim(),
                code.trim().toUpperCase(),
                facilityType,
                address,
                managerId,
                WarehouseStatus.ACTIVE,
                now
        );

        warehouse.registerEvent(WarehouseCreatedEvent.of(
                id,
                correlationId,
                new WarehouseCreatedEvent.Payload(
                        id,
                        organizationId,
                        warehouse.name,
                        warehouse.code,
                        facilityType.name(),
                        now
                )
        ));

        return warehouse;
    }

    public void deactivate() {
        if (this.status == WarehouseStatus.INACTIVE) {
            throw new WarehousingInvariantException("Warehouse is already inactive");
        }
        this.status = WarehouseStatus.INACTIVE;
    }

    public void reactivate() {
        if (this.status == WarehouseStatus.ACTIVE) {
            throw new WarehousingInvariantException("Warehouse is already active");
        }
        this.status = WarehouseStatus.ACTIVE;
    }

    public void updateDetails(String name, Address address, Long managerId) {
        if (name != null && !name.isBlank()) {
            this.name = name.trim();
        }
        if (address != null) {
            this.address = address;
        }
        this.managerId = managerId;
    }

    @Override
    public Long getId() {
        return id;
    }
}
