package com.atlashub.catalog.application.result;

import com.atlashub.catalog.domain.model.ProductPricing;
import com.atlashub.catalog.domain.valueobject.BillingCycle;
import com.atlashub.shared.domain.valueobject.Money;

public record SetProductPricingResult(
        Long pricingId,
        Long hubProductId,
        BillingCycle billingCycle,
        Money amount,
        boolean wasUpdated
) {
    public static SetProductPricingResult created(ProductPricing pricing) {
        return new SetProductPricingResult(
                pricing.getId(),
                pricing.getHubProductId(),
                pricing.getBillingCycle(),
                pricing.getAmount(),
                false
        );
    }

    public static SetProductPricingResult updated(ProductPricing pricing) {
        return new SetProductPricingResult(
                pricing.getId(),
                pricing.getHubProductId(),
                pricing.getBillingCycle(),
                pricing.getAmount(),
                true
        );
    }
}
