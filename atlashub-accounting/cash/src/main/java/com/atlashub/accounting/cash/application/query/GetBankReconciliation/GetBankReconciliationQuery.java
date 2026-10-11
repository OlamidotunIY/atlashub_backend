package com.atlashub.accounting.cash.application.query.GetBankReconciliation;

public record GetBankReconciliationQuery(
        Long organizationId,
        Long reconciliationId
) {}
