package com.atlashub.billing.application.command.CancelSubscription;

public record CancelSubscriptionResult(
        Long subscriptionId,
        String status
) {
}
