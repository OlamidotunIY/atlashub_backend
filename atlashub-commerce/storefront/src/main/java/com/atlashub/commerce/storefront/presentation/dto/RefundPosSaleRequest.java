package com.atlashub.commerce.storefront.presentation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record RefundPosSaleRequest(
        @NotNull Long salesOrderId,
        @NotBlank String reason,
        String customerNuban,
        String customerBankCode
) {
}
