package com.atlashub.billing.application.command.ActivateSubscription;

import java.time.LocalDate;

public record ActivateSubscriptionCommand(
        Long subscriptionId,
        LocalDate periodStart,
        LocalDate periodEnd
) {
}
