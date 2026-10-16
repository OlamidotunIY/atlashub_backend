package com.atlashub.accounting.cash.presentation.dto;

import com.atlashub.accounting.cash.domain.valueobject.StatementEntryType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDate;

public record BankStatementLineDto(
        @NotNull(message = "Transaction date is required")
        LocalDate transactionDate,

        @NotBlank(message = "Description cannot be blank")
        String description,

        @NotNull(message = "Amount is required")
        @Positive(message = "Amount must be positive")
        BigDecimal amount,

        @NotNull(message = "Entry type is required")
        StatementEntryType type
) {}
