package com.atlashub.accounting.ap.application.command.MarkSupplierInvoicePaid;

import java.time.ZonedDateTime;

public record MarkSupplierInvoicePaidCommand(
        Long invoiceId,
        String payoutReference,
        ZonedDateTime paidAt
) {
    public MarkSupplierInvoicePaidCommand(Long invoiceId, String payoutReference) {
        this(invoiceId, payoutReference, ZonedDateTime.now());
    }
}
