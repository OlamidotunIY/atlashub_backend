package com.atlashub.logistics.shipping.application.commands.CreateShipment;

import com.atlashub.logistics.shipping.domain.valueobject.Address;
import com.atlashub.logistics.shipping.domain.valueobject.DeliveryTier;
import com.atlashub.logistics.shipping.domain.valueobject.Dimensions;
import com.atlashub.logistics.shipping.domain.valueobject.ShipmentType;
import com.atlashub.shared.domain.valueobject.Money;
import java.math.BigDecimal;

public record CreateShipmentCommand(
        Long organizationId,
        Long orderId,
        ShipmentType type,
        DeliveryTier serviceTier,
        Address originAddress,
        Address destinationAddress,
        Long senderId,
        Long receiverId,
        BigDecimal weightKg,
        Dimensions dimensions,
        Money declaredValue,
        String correlationId
) {}
