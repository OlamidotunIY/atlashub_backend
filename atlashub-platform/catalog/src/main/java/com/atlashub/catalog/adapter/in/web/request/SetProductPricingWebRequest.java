package com.atlashub.catalog.adapter.in.web.request;

import com.atlashub.catalog.domain.valueobject.BillingCycle;
import com.atlashub.shared.domain.valueobject.CurrencyCode;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record SetProductPricingWebRequest(
        @NotNull BillingCycle cycle,
        @NotNull BigDecimal amount,
        @NotNull CurrencyCode currency
) {
}
