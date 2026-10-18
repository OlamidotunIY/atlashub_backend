package com.atlashub.billing.application.command.RestoreSubscription;

public record RestoreSubscriptionResult(
        Long subscriptionId,
        String status
) {
}
