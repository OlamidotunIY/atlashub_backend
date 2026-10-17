package com.atlashub.shared.application.port;

import java.time.ZonedDateTime;
import java.util.Optional;

public interface LogisticsShippingQueryPort {

    record ShippingSnapshot(
            Long shipmentId,
            Long organizationId,
            Long orderId,
            String trackingNumber,
            String status,
            String serviceTier,
            String routeType,
            String currentMilestone,
            String deliveryCode,
            ZonedDateTime deliveryWindowStart,
            ZonedDateTime deliveryWindowEnd
    ) {}

    Optional<ShippingSnapshot> findShipmentById(Long shipmentId);

    Optional<ShippingSnapshot> findShipmentByTrackingNumber(String trackingNumber);
}
