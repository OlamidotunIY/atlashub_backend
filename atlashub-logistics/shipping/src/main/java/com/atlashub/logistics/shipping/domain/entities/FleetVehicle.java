package com.atlashub.logistics.shipping.domain.entities;

import com.atlashub.logistics.shipping.domain.exceptions.VehicleUnavailableException;
import com.atlashub.logistics.shipping.domain.valueobject.VehicleStatus;
import com.atlashub.logistics.shipping.domain.valueobject.VehicleType;
import com.atlashub.shared.domain.entities.AggregateRoot;
import java.math.BigDecimal;
import java.time.ZonedDateTime;
import lombok.Getter;

@Getter
public class FleetVehicle extends AggregateRoot<Long> {

    private final Long id;
    private final Long organizationId;
    private final String zoneCode;
    private final String licensePlate;
    private final VehicleType vehicleType;
    private final BigDecimal maxWeightKg;
    private VehicleStatus status;
    private Long currentRiderId;
    private final ZonedDateTime registeredAt;

    public FleetVehicle(
            Long id,
            Long organizationId,
            String zoneCode,
            String licensePlate,
            VehicleType vehicleType,
            BigDecimal maxWeightKg,
            VehicleStatus status,
            Long currentRiderId,
            ZonedDateTime registeredAt
    ) {
        this.id = id;
        this.organizationId = organizationId;
        this.zoneCode = zoneCode;
        this.licensePlate = licensePlate;
        this.vehicleType = vehicleType;
        this.maxWeightKg = maxWeightKg;
        this.status = status;
        this.currentRiderId = currentRiderId;
        this.registeredAt = registeredAt;
    }

    public static FleetVehicle create(
            Long id,
            Long organizationId,
            String zoneCode,
            String licensePlate,
            VehicleType vehicleType,
            BigDecimal maxWeightKg
    ) {
        return new FleetVehicle(
                id,
                organizationId,
                zoneCode,
                licensePlate,
                vehicleType,
                maxWeightKg,
                VehicleStatus.ACTIVE,
                null,
                ZonedDateTime.now()
        );
    }

    public void assignRider(Long riderId) {
        if (this.status != VehicleStatus.ACTIVE) {
            throw new VehicleUnavailableException("Cannot assign rider to vehicle with status: " + this.status);
        }
        this.currentRiderId = riderId;
    }

    public void unassignRider() {
        this.currentRiderId = null;
    }

    public void markMaintenance() {
        this.status = VehicleStatus.MAINTENANCE;
        this.currentRiderId = null;
    }

    public void returnToService() {
        this.status = VehicleStatus.ACTIVE;
    }

    @Override
    public Long getId() {
        return id;
    }
}
