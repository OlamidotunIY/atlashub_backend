package com.atlashub.billing.application.command.CancelSubscription;

public record CancelSubscriptionCommand(
        Long subscriptionId,
        String reason
) {
}
