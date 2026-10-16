package com.atlashub.billing.application.command.ChangePlan;

public record ChangePlanCommand(
        Long subscriptionId,
        Long newPricingPlanId
) {
}
