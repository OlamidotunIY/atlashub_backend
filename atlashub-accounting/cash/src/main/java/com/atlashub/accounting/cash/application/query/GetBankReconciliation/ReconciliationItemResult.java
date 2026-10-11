package com.atlashub.accounting.cash.application.query.GetBankReconciliation;

public record ReconciliationItemResult(
        Long id,
        Long reconciliationId,
        Long bankStatementLineId,
        Long journalLineId
) {}
