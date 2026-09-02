package com.atlashub.catalog.application.result;

import com.atlashub.catalog.domain.model.HubProduct;
import com.atlashub.catalog.domain.model.ProductPricing;
import com.atlashub.catalog.domain.valueobject.BillingCycle;
import com.atlashub.catalog.domain.valueobject.ProductKey;
import com.atlashub.catalog.domain.valueobject.ProductStatus;
import com.atlashub.shared.money.Money;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.stream.Collectors;

public record HubProductDetailsResult(
        Long id,
        ProductKey key,
        String name,
        String description,
        ProductStatus status,
        ZonedDateTime createdAt,
        ZonedDateTime updatedAt,
        List<PricingResult> pricing
) {
    public record PricingResult(
            Long pricingId,
            BillingCycle billingCycle,
            Money amount
    ) {
        public static PricingResult from(ProductPricing pricing) {
            return new PricingResult(
                    pricing.getId(),
                    pricing.getBillingCycle(),
                    pricing.getAmount()
            );
        }
    }

    public static HubProductDetailsResult from(HubProduct product, List<ProductPricing> pricingList) {
        return new HubProductDetailsResult(
                product.getId(),
                product.getKey(),
                product.getName(),
                product.getDescription(),
                product.getStatus(),
                product.getCreatedAt(),
                product.getUpdatedAt(),
                pricingList.stream().map(PricingResult::from).collect(Collectors.toList())
        );
    }
}
