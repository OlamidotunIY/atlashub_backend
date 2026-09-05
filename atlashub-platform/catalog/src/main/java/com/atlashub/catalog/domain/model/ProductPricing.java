package com.atlashub.catalog.domain.model;

import com.atlashub.catalog.domain.valueobject.BillingCycle;
import com.atlashub.shared.domain.AggregateRoot;
import com.atlashub.shared.domain.money.Money;
import lombok.Getter;

import java.time.ZonedDateTime;

import com.atlashub.catalog.domain.events.ProductPricingUpdatedEvent;
import java.util.UUID;

@Getter
public class ProductPricing extends AggregateRoot<Long> {
    private final Long id;
    private final Long hubProductId;
    private final BillingCycle billingCycle;
    private Money amount;
    private final ZonedDateTime createdAt;
    private ZonedDateTime updatedAt;

    public ProductPricing(Long id, Long hubProductId, BillingCycle billingCycle, Money amount, ZonedDateTime createdAt, ZonedDateTime updatedAt) {
        this.id = id;
        this.hubProductId = hubProductId;
        this.billingCycle = billingCycle;
        this.amount = amount;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static ProductPricing create(Long id, Long hubProductId, BillingCycle billingCycle, Money amount) {
        ProductPricing pricing = new ProductPricing(id, hubProductId, billingCycle, amount, ZonedDateTime.now(), ZonedDateTime.now());
        
        pricing.registerEvent(
                new ProductPricingUpdatedEvent(
                        UUID.randomUUID().toString(),
                        String.valueOf(id),
                        ZonedDateTime.now(),
                        new ProductPricingUpdatedEvent.Payload(hubProductId, billingCycle, amount)
                )
        );
        
        return pricing;
    }

    public void updatePrice(Money newAmount) {
        this.amount = newAmount;
        this.updatedAt = ZonedDateTime.now();
        
        this.registerEvent(
                new ProductPricingUpdatedEvent(
                        UUID.randomUUID().toString(),
                        String.valueOf(id),
                        ZonedDateTime.now(),
                        new ProductPricingUpdatedEvent.Payload(hubProductId, billingCycle, newAmount)
                )
        );
    }

    @Override
    public Long getId() {
        return id;
    }
}
