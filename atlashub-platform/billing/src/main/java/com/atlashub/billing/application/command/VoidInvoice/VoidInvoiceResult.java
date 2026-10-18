package com.atlashub.billing.application.command.VoidInvoice;

public record VoidInvoiceResult(
        Long invoiceId,
        String status
) {
}
