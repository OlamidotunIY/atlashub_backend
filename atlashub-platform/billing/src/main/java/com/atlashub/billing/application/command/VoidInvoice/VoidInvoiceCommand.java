package com.atlashub.billing.application.command.VoidInvoice;

public record VoidInvoiceCommand(
        Long invoiceId,
        String reason
) {
}
