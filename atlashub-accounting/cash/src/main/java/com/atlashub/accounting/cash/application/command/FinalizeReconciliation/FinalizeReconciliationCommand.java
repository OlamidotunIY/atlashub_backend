package com.atlashub.accounting.cash.application.command.FinalizeReconciliation;

public record FinalizeReconciliationCommand(
        Long organizationId,
        Long reconciliationId,
        boolean forceFinalize
) {}
