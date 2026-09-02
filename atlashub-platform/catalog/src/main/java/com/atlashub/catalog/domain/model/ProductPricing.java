package com.atlashub.catalog.domain.model;

import com.atlashub.catalog.domain.valueobject.BillingCycle;
import com.atlashub.shared.domain.AggregateRoot;
import com.atlashub.shared.money.Money;
import jakarta.persistence.Version;
import lombok.Getter;

@Getter
public class ProductPricing extends AggregateRoot<Long> {
    private final Long id;
    private final Long hubProductId;
    private final BillingCycle billingCycle;
    private Money amount;
    @Version
    private Long version;

    public ProductPricing(Long id, Long hubProductId, BillingCycle billingCycle, Money amount) {
        this.id = id;
        this.hubProductId = hubProductId;
        this.billingCycle = billingCycle;
        this.amount = amount;
    }

    public void updatePrice(Money newAmount) {
        this.amount = newAmount;
    }

    @Override
    public Long getId() {
        return id;
    }
}
