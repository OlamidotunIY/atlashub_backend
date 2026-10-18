package com.atlashub.logistics.shipping.domain.entities;

import com.atlashub.logistics.shipping.domain.valueobject.RouteType;
import com.atlashub.logistics.shipping.domain.valueobject.ShipmentStatus;
import com.atlashub.logistics.shipping.domain.valueobject.TrackingMilestone;
import com.atlashub.shared.domain.entities.AggregateRoot;
import java.math.BigDecimal;
import java.time.ZonedDateTime;
import lombok.Getter;

@Getter
public class ShipmentMilestoneLog extends AggregateRoot<Long> {

    private final Long id;
    private final Long shipmentId;
    private final String trackingNumber;
    private final RouteType routeType;
    private final TrackingMilestone milestone;
    private final ShipmentStatus status;
    private final String locationName;
    private final String state;
    private final String description;
    private final BigDecimal latitude;
    private final BigDecimal longitude;
    private final ZonedDateTime occurredAt;

    public ShipmentMilestoneLog(
            Long id,
            Long shipmentId,
            String trackingNumber,
            RouteType routeType,
            TrackingMilestone milestone,
            ShipmentStatus status,
            String locationName,
            String state,
            String description,
            BigDecimal latitude,
            BigDecimal longitude,
            ZonedDateTime occurredAt
    ) {
        this.id = id;
        this.shipmentId = shipmentId;
        this.trackingNumber = trackingNumber;
        this.routeType = routeType;
        this.milestone = milestone;
        this.status = status;
        this.locationName = locationName;
        this.state = state;
        this.description = description;
        this.latitude = latitude;
        this.longitude = longitude;
        this.occurredAt = occurredAt;
    }

    public static ShipmentMilestoneLog create(
            Long id,
            Long shipmentId,
            String trackingNumber,
            RouteType routeType,
            TrackingMilestone milestone,
            ShipmentStatus status,
            String locationName,
            String state,
            String description,
            BigDecimal latitude,
            BigDecimal longitude
    ) {
        return new ShipmentMilestoneLog(
                id,
                shipmentId,
                trackingNumber,
                routeType,
                milestone,
                status,
                locationName,
                state,
                description,
                latitude,
                longitude,
                ZonedDateTime.now()
        );
    }

    @Override
    public Long getId() {
        return id;
    }
}
