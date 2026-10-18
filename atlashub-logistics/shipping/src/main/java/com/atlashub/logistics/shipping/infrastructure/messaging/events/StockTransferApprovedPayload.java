package com.atlashub.logistics.shipping.infrastructure.messaging.events;

import com.atlashub.logistics.shipping.domain.valueobject.Address;
import com.atlashub.logistics.shipping.domain.valueobject.Dimensions;
import com.atlashub.shared.domain.valueobject.Money;
import java.math.BigDecimal;

public record StockTransferApprovedPayload(
        Long organizationId,
        Long stockTransferId,
        Address sourceAddress,
        Address destinationAddress,
        Long requestedBy,
        BigDecimal totalWeightKg,
        Money declaredValue,
        String correlationId
) {}
