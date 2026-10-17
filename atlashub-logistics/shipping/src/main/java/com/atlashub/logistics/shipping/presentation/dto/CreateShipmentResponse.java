package com.atlashub.logistics.shipping.presentation.dto;

import com.atlashub.shared.domain.valueobject.Money;
import java.time.ZonedDateTime;

public record CreateShipmentResponse(
        Long shipmentId,
        String trackingNumber,
        String deliveryCode,
        String routeType,
        String serviceTier,
        Money customerShippingFee,
        ZonedDateTime deliveryWindowStart,
        ZonedDateTime deliveryWindowEnd
) {}
