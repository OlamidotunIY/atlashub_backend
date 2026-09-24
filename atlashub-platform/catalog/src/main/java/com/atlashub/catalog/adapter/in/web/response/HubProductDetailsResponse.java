package com.atlashub.catalog.adapter.in.web.response;

import java.math.BigDecimal;
import java.util.List;

public record HubProductDetailsResponse(
        Long id,
        String key,
        String name,
        String description,
        String status,
        List<Pricing> pricing
) {
    public record Pricing(
            Long id,
            String cycle,
            BigDecimal amount,
            String currency
    ) {}
}
