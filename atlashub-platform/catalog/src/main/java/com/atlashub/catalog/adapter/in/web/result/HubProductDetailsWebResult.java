package com.atlashub.catalog.adapter.in.web.result;

import com.atlashub.catalog.application.result.HubProductDetailsResult;
import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.stream.Collectors;

public record HubProductDetailsWebResult(
        Long id,
        String key,
        String name,
        String description,
        String status,
        ZonedDateTime createdAt,
        ZonedDateTime updatedAt,
        List<PricingWebResult> pricing
) {
    public record PricingWebResult(
            Long pricingId,
            String billingCycle,
            BigDecimal amount,
            String currency
    ) {
        public static PricingWebResult from(HubProductDetailsResult.PricingResult appResult) {
            return new PricingWebResult(
                    appResult.pricingId(),
                    appResult.billingCycle().name(),
                    appResult.amount().amount(),
                    appResult.amount().currency().name()
            );
        }
    }

    public static HubProductDetailsWebResult from(HubProductDetailsResult appResult) {
        return new HubProductDetailsWebResult(
                appResult.id(),
                appResult.key().name(),
                appResult.name(),
                appResult.description(),
                appResult.status().name(),
                appResult.createdAt(),
                appResult.updatedAt(),
                appResult.pricing().stream().map(PricingWebResult::from).collect(Collectors.toList())
        );
    }
}
