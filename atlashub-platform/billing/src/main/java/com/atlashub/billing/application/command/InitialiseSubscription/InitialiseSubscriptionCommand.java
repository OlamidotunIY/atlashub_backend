package com.atlashub.billing.application.command.InitialiseSubscription;

public record InitialiseSubscriptionCommand(
        Long organizationId,
        Long pricingPlanId,
        String currency
) {
}
