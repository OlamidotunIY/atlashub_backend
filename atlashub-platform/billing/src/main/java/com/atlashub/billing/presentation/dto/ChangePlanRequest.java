package com.atlashub.billing.presentation.dto;

import jakarta.validation.constraints.NotNull;

public record ChangePlanRequest(
        @NotNull(message = "Pricing plan ID is required")
        Long newPricingPlanId
) {
}
