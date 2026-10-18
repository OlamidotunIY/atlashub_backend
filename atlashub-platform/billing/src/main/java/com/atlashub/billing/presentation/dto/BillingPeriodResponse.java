package com.atlashub.billing.presentation.dto;

import java.time.LocalDate;

public record BillingPeriodResponse(
        Long subscriptionId,
        LocalDate currentPeriodStart,
        LocalDate currentPeriodEnd,
        LocalDate nextBillingDate,
        String status
) {
}
