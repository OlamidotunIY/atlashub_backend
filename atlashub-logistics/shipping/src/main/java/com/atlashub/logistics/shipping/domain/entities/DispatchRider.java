package com.atlashub.logistics.shipping.domain.entities;

import com.atlashub.logistics.shipping.domain.exceptions.RiderAlreadyOnDeliveryException;
import com.atlashub.logistics.shipping.domain.exceptions.RiderUnavailableException;
import com.atlashub.logistics.shipping.domain.valueobject.RiderStatus;
import com.atlashub.shared.domain.entities.AggregateRoot;
import java.time.ZonedDateTime;
import lombok.Getter;

@Getter
public class DispatchRider extends AggregateRoot<Long> {

    private final Long id;
    private final Long organizationId;
    private final Long userId;
    private final String zoneCode;
    private Long vehicleId;
    private final String licenseNumber;
    private RiderStatus status;
    private Long currentShipmentId;
    private final ZonedDateTime onboardedAt;

    public DispatchRider(
            Long id,
            Long organizationId,
            Long userId,
            String zoneCode,
            Long vehicleId,
            String licenseNumber,
            RiderStatus status,
            Long currentShipmentId,
            ZonedDateTime onboardedAt
    ) {
        this.id = id;
        this.organizationId = organizationId;
        this.userId = userId;
        this.zoneCode = zoneCode;
        this.vehicleId = vehicleId;
        this.licenseNumber = licenseNumber;
        this.status = status;
        this.currentShipmentId = currentShipmentId;
        this.onboardedAt = onboardedAt;
    }

    public static DispatchRider create(
            Long id,
            Long organizationId,
            Long userId,
            String zoneCode,
            Long vehicleId,
            String licenseNumber
    ) {
        return new DispatchRider(
                id,
                organizationId,
                userId,
                zoneCode,
                vehicleId,
                licenseNumber,
                RiderStatus.AVAILABLE,
                null,
                ZonedDateTime.now()
        );
    }

    public void goOnline() {
        if (this.status != RiderStatus.ON_DELIVERY) {
            this.status = RiderStatus.AVAILABLE;
        }
    }

    public void goOffline() {
        if (this.status == RiderStatus.ON_DELIVERY) {
            throw new RiderAlreadyOnDeliveryException();
        }
        this.status = RiderStatus.OFFLINE;
    }

    public void assignDelivery(Long shipmentId, Long vehicleId) {
        if (this.status != RiderStatus.AVAILABLE) {
            throw new RiderUnavailableException();
        }
        this.currentShipmentId = shipmentId;
        if (vehicleId != null) {
            this.vehicleId = vehicleId;
        }
        this.status = RiderStatus.ON_DELIVERY;
    }

    public void completeDelivery() {
        this.currentShipmentId = null;
        this.status = RiderStatus.AVAILABLE;
    }

    @Override
    public Long getId() {
        return id;
    }
}
