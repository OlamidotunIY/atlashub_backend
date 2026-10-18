package com.atlashub.billing.application.command.InitialiseSubscription;

public record InitialiseSubscriptionResult(
        Long subscriptionId,
        Long organizationId,
        String status
) {
}
