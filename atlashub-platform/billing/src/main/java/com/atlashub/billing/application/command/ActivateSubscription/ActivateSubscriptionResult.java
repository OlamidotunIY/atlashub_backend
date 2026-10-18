package com.atlashub.billing.application.command.ActivateSubscription;

public record ActivateSubscriptionResult(
        Long subscriptionId,
        String status
) {
}
