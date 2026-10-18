package com.atlashub.billing.application.query.GetSubscription;

import java.time.LocalDate;
import java.time.ZonedDateTime;

public record SubscriptionResult(
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
