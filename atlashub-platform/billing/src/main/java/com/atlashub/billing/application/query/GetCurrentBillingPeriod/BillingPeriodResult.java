package com.atlashub.billing.application.query.GetCurrentBillingPeriod;

import java.time.LocalDate;

public record BillingPeriodResult(
        Long subscriptionId,
        LocalDate currentPeriodStart,
        LocalDate currentPeriodEnd,
        LocalDate nextBillingDate,
        String status
) {
}
