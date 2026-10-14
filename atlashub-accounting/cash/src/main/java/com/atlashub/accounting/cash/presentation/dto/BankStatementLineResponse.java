package com.atlashub.accounting.cash.presentation.dto;

import com.atlashub.accounting.cash.domain.valueobject.StatementEntryType;

import java.math.BigDecimal;
import java.time.LocalDate;

public record BankStatementLineResponse(
        Long id,
        Long reconciliationId,
        LocalDate transactionDate,
        String description,
        BigDecimal amount,
        String currency,
        StatementEntryType type
) {}
