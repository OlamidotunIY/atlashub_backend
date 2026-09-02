package com.atlashub.catalog.domain.model;

import com.atlashub.catalog.domain.valueobject.BillingCycle;
import com.atlashub.shared.domain.AggregateRoot;
import com.atlashub.shared.money.Money;
import lombok.Getter;

import java.time.ZonedDateTime;

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
        return new ProductPricing(id, hubProductId, billingCycle, amount, ZonedDateTime.now(), ZonedDateTime.now());
    }

    public void updatePrice(Money newAmount) {
        this.amount = newAmount;
        this.updatedAt = ZonedDateTime.now();
    }

    @Override
    public Long getId() {
        return id;
    }
}
