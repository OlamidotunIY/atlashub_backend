package com.atlashub.accounting.cash.presentation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record RecordCashEvacuationRequest(
        @NotNull(message = "Outlet ID is required")
        @Positive(message = "Outlet ID must be positive")
        Long outletId,

        @NotNull(message = "Till ID is required")
        @Positive(message = "Till ID must be positive")
        Long tillId,

        @NotNull(message = "Amount is required")
        @Positive(message = "Amount must be positive")
        BigDecimal amount,

        @NotBlank(message = "Currency is required")
        String currency
) {}
