package com.atlashub.billing.application.command.MarkInvoiceOverdue;

public record MarkInvoiceOverdueResult(
        Long invoiceId,
        String status
) {
}
