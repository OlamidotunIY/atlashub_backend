package com.atlashub.billing.application.command.RenewSubscription;

import java.time.LocalDate;

public record RenewSubscriptionResult(
        Long subscriptionId,
        String status,
        LocalDate currentPeriodStart,
        LocalDate currentPeriodEnd
) {
}
