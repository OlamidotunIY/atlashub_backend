package com.atlashub.catalog.adapter.in.web.response;

import com.atlashub.catalog.domain.valueobject.BillingCycle;
import com.atlashub.catalog.domain.valueobject.ProductKey;
import com.atlashub.catalog.domain.valueobject.ProductStatus;
import com.atlashub.shared.domain.money.CurrencyCode;
import java.math.BigDecimal;
import java.util.List;

public record HubProductDetailsResponse(
        Long id,
        ProductKey key,
        String name,
        String description,
        ProductStatus status,
        List<Pricing> pricing
) {
    public record Pricing(
            Long id,
            BillingCycle cycle,
            BigDecimal amount,
            CurrencyCode currency
    ) {}
}
