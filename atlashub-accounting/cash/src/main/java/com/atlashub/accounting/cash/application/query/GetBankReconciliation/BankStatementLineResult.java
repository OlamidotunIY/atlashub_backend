package com.atlashub.accounting.cash.application.query.GetBankReconciliation;

import com.atlashub.accounting.cash.domain.valueobject.StatementEntryType;
import com.atlashub.shared.domain.valueobject.Money;

import java.time.LocalDate;

public record BankStatementLineResult(
        Long id,
        Long reconciliationId,
        LocalDate transactionDate,
        String description,
        Money amount,
        StatementEntryType type
) {}
