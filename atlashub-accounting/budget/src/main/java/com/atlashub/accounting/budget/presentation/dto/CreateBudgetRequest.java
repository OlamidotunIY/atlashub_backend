package com.atlashub.accounting.budget.presentation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record CreateBudgetRequest(
        @NotNull(message = "Account ID is required")
        Long accountId,

        @NotBlank(message = "Budget name is required")
        @Size(max = 128, message = "Budget name must not exceed 128 characters")
        String name,

        @NotBlank(message = "Period is required")
        @Size(max = 32, message = "Period must not exceed 32 characters")
        String period,

        @NotNull(message = "Budgeted amount is required")
        @Positive(message = "Budgeted amount must be greater than zero")
        BigDecimal budgetedAmount,

        @NotBlank(message = "Currency is required")
        @Size(min = 3, max = 3, message = "Currency must be a 3-letter ISO code")
        String currency
) {}
