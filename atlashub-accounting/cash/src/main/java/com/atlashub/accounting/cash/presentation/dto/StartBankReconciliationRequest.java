package com.atlashub.accounting.cash.presentation.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record StartBankReconciliationRequest(
        @NotBlank(message = "Bank account code is required")
        String bankAccountCode,

        @NotNull(message = "Statement date is required")
        LocalDate statementDate,

        @NotNull(message = "Statement closing balance is required")
        BigDecimal statementClosingBalance,

        @NotBlank(message = "Currency is required")
        String currency,

        @NotNull(message = "Bank statement lines cannot be null")
        @NotEmpty(message = "At least one bank statement line is required")
        @Valid
        List<BankStatementLineDto> bankLines
) {}
