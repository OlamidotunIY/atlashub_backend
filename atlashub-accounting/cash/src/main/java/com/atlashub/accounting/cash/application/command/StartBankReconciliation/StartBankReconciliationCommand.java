package com.atlashub.accounting.cash.application.command.StartBankReconciliation;

import com.atlashub.shared.domain.valueobject.Money;

import java.time.LocalDate;
import java.util.List;

public record StartBankReconciliationCommand(
        Long organizationId,
        String bankAccountCode,
        LocalDate statementDate,
        Money statementClosingBalance,
        List<BankStatementLineInput> bankLines
) {}
