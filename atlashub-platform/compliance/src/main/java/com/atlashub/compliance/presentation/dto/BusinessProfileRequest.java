package com.atlashub.compliance.presentation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record BusinessProfileRequest(
        @NotBlank String businessDescription,
        @NotBlank String industry,
        @NotBlank String annualTransactionVolume,
        @NotNull @Positive BigDecimal expectedMonthlyVolume,
        @NotBlank String currency,
        @NotNull @Positive Integer staffCount
) {
}
