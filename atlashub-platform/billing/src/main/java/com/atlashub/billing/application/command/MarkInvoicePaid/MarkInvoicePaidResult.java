package com.atlashub.billing.application.command.MarkInvoicePaid;

public record MarkInvoicePaidResult(
        Long invoiceId,
        String status
) {
}
