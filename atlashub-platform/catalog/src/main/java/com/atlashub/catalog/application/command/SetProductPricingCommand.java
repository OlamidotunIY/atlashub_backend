package com.atlashub.catalog.application.command;

import com.atlashub.catalog.domain.valueobject.BillingCycle;
import com.atlashub.shared.domain.money.Money;

public record SetProductPricingCommand(Long productId, BillingCycle cycle, Money amount) {
}
