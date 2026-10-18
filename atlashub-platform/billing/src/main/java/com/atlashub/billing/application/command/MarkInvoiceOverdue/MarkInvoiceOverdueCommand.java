package com.atlashub.billing.application.command.MarkInvoiceOverdue;

public record MarkInvoiceOverdueCommand(
        Long invoiceId
) {
}
