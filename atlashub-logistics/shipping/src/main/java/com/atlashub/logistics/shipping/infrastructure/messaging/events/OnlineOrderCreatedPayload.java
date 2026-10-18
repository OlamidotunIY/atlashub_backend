package com.atlashub.logistics.shipping.infrastructure.messaging.events;

import com.atlashub.logistics.shipping.domain.valueobject.Address;
import com.atlashub.logistics.shipping.domain.valueobject.DeliveryTier;
import com.atlashub.logistics.shipping.domain.valueobject.Dimensions;
import com.atlashub.shared.domain.valueobject.Money;
import java.math.BigDecimal;

public record OnlineOrderCreatedPayload(
        Long organizationId,
        Long orderId,
        DeliveryTier serviceTier,
        Address originAddress,
        Address destinationAddress,
        Long customerId,
        BigDecimal weightKg,
        Dimensions dimensions,
        Money declaredValue,
        String correlationId
) {}
