package com.atlashub.pay.charges.presentation.dto;

import jakarta.validation.constraints.NotBlank;

public record RefundChargeRequest(
        @NotBlank String reason
) {
}
