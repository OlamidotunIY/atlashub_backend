package com.atlashub.logistics.shipping.application.queries.TrackShipment;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;

public record ShipmentTrackingResult(
        String trackingNumber,
        String status,
        String currentMilestone,
        String routeType,
        String currentLocation,
        BigDecimal currentLatitude,
        BigDecimal currentLongitude,
        ZonedDateTime deliveryWindowStart,
        ZonedDateTime deliveryWindowEnd,
        List<MilestoneEntry> milestoneHistory
) {
    public record MilestoneEntry(
            String milestone,
            String status,
            String locationName,
            String state,
            String description,
            BigDecimal latitude,
            BigDecimal longitude,
            ZonedDateTime occurredAt
    ) {}
}
