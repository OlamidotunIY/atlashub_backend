package com.atlashub.billing.presentation.dto;

import java.time.LocalDate;
import java.time.ZonedDateTime;

public record SubscriptionResponse(
        Long id,
        Long organizationId,
        Long pricingPlanId,
        String status,
        LocalDate currentPeriodStart,
        LocalDate currentPeriodEnd,
        LocalDate nextBillingDate,
        String billingCurrency,
        LocalDate trialEndsAt,
        ZonedDateTime cancelledAt,
        String cancelReason,
        ZonedDateTime createdAt,
        ZonedDateTime updatedAt
) {
}
