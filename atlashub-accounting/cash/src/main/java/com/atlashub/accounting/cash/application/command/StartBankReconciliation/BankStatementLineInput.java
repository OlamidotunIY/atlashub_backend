package com.atlashub.accounting.cash.application.command.StartBankReconciliation;

import com.atlashub.accounting.cash.domain.valueobject.StatementEntryType;
import com.atlashub.shared.domain.valueobject.Money;

import java.time.LocalDate;

public record BankStatementLineInput(
        LocalDate transactionDate,
        String description,
        Money amount,
        StatementEntryType type
) {}
