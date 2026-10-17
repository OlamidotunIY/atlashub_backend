package com.atlashub.logistics.shipping.application.queries.CalculateShippingQuotes;

import com.atlashub.logistics.shipping.domain.valueobject.Address;
import com.atlashub.shared.domain.valueobject.Money;
import java.math.BigDecimal;

public record CalculateShippingQuotesQuery(
        Long organizationId,
        Address originAddress,
        Address destinationAddress,
        BigDecimal weightKg,
        Money declaredValue
) {}
