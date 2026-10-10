package com.atlashub.pay.charges.application.commands.ReconcilePendingCharges;

public record ReconcilePendingChargesResult(
        int reconciledCount,
        int failureCount
) {
}
