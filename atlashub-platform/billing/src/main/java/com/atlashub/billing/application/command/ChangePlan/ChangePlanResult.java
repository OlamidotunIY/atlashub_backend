package com.atlashub.billing.application.command.ChangePlan;

public record ChangePlanResult(
        Long subscriptionId,
        Long pricingPlanId,
        String status
) {
}
