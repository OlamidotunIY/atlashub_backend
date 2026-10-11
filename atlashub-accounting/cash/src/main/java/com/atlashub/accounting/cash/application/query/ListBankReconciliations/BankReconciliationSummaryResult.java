package com.atlashub.accounting.cash.application.query.ListBankReconciliations;

import com.atlashub.accounting.cash.domain.valueobject.ReconciliationStatus;
import com.atlashub.shared.domain.valueobject.Money;

import java.time.LocalDate;
import java.time.ZonedDateTime;

public record BankReconciliationSummaryResult(
        Long id,
        Long organizationId,
        String bankAccountCode,
        LocalDate statementDate,
        Money statementClosingBalance,
        Money bookBalance,
        Money difference,
        ReconciliationStatus status,
        int matchedItemsCount,
        int unmatchedBankItemsCount,
        int unmatchedBookItemsCount,
        ZonedDateTime createdAt,
        ZonedDateTime reconciledAt
) {}
