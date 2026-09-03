package com.atlashub.billing.application.port.out;

import java.math.BigDecimal;

public record ProductPricingDto(
        Long productId,
        BigDecimal priceAmount,
        String currency,
        String billingCycle
) {}