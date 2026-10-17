package com.atlashub.logistics.shipping.application.commands.CreateShipment;

import com.atlashub.shared.domain.valueobject.Money;
import java.time.ZonedDateTime;

public record CreateShipmentResult(
        Long shipmentId,
        String trackingNumber,
        String deliveryCode,
        String routeType,
        String serviceTier,
        Money customerShippingFee,
        ZonedDateTime deliveryWindowStart,
        ZonedDateTime deliveryWindowEnd
) {}
