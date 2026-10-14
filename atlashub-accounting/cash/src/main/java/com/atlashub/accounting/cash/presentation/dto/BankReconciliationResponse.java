package com.atlashub.accounting.cash.presentation.dto;

import com.atlashub.accounting.cash.domain.valueobject.ReconciliationStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record BankReconciliationResponse(
        Long id,
        Long organizationId,
        String bankAccountCode,
        LocalDate statementDate,
        BigDecimal statementClosingBalance,
        BigDecimal bookBalance,
        BigDecimal difference,
        String currency,
        ReconciliationStatus status,
        List<MatchedItemResponse> matchedItems,
        List<BankStatementLineResponse> unmatchedBankItems,
        List<Long> unmatchedBookItems
) {}
