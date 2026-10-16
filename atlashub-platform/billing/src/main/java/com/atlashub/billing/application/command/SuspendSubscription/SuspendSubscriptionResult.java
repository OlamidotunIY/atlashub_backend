package com.atlashub.billing.application.command.SuspendSubscription;

public record SuspendSubscriptionResult(
        Long subscriptionId,
        String status
) {
}
