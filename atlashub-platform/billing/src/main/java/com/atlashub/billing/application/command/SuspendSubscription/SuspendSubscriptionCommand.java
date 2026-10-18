package com.atlashub.billing.application.command.SuspendSubscription;

public record SuspendSubscriptionCommand(
        Long subscriptionId,
        String reason
) {
}
