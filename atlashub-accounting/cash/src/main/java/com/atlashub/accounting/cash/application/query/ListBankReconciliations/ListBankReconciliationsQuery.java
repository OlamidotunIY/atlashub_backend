package com.atlashub.accounting.cash.application.query.ListBankReconciliations;

import com.atlashub.accounting.cash.domain.valueobject.ReconciliationStatus;

public record ListBankReconciliationsQuery(
        Long organizationId,
        ReconciliationStatus status
) {}
