package com.atlashub.catalog.adapter.in.web.request;

import java.math.BigDecimal;

public record SetProductPricingWebRequest(
        String cycle,
        BigDecimal amount,
        String currency
) {
}
