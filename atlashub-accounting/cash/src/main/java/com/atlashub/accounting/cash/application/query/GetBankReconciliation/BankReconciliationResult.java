package com.atlashub.accounting.cash.application.query.GetBankReconciliation;

import com.atlashub.accounting.cash.domain.valueobject.ReconciliationStatus;
import com.atlashub.shared.domain.valueobject.Money;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.List;

public record BankReconciliationResult(
        Long id,
        Long organizationId,
        String bankAccountCode,
        LocalDate statementDate,
        Money statementClosingBalance,
        Money bookBalance,
        Money difference,
        ReconciliationStatus status,
        List<ReconciliationItemResult> matchedItems,
        List<BankStatementLineResult> unmatchedBankItems,
        List<Long> unmatchedBookItems,
        ZonedDateTime createdAt,
        ZonedDateTime reconciledAt
) {}
