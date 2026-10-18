package com.atlashub.logistics.shipping.application.queries.ListShipments;

import com.atlashub.shared.domain.valueobject.Money;
import java.time.ZonedDateTime;

public record ShipmentSummaryResult(
        Long id,
        Long organizationId,
        Long orderId,
        String trackingNumber,
        String type,
        String serviceTier,
        String routeType,
        String originCity,
        String destinationCity,
        String currentMilestone,
        String status,
        Money customerShippingFee,
        ZonedDateTime deliveryWindowStart,
        ZonedDateTime deliveryWindowEnd,
        ZonedDateTime createdAt
) {}
