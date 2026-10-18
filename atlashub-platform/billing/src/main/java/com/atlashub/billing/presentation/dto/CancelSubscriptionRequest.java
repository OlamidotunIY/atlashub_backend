package com.atlashub.billing.presentation.dto;

import jakarta.validation.constraints.NotBlank;

public record CancelSubscriptionRequest(
        @NotBlank(message = "Cancellation reason is required")
        String reason
) {
}
