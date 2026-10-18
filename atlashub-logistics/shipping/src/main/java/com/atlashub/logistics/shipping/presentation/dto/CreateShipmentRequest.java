package com.atlashub.logistics.shipping.presentation.dto;

import com.atlashub.logistics.shipping.domain.valueobject.Address;
import com.atlashub.logistics.shipping.domain.valueobject.DeliveryTier;
import com.atlashub.logistics.shipping.domain.valueobject.Dimensions;
import com.atlashub.logistics.shipping.domain.valueobject.ShipmentType;
import com.atlashub.shared.domain.valueobject.Money;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record CreateShipmentRequest(
        Long orderId,
        ShipmentType type,
        DeliveryTier serviceTier,
        @NotNull Address originAddress,
        @NotNull Address destinationAddress,
        Long senderId,
        Long receiverId,
        BigDecimal weightKg,
        Dimensions dimensions,
        Money declaredValue
) {}
