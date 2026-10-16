package com.atlashub.accounting.ap.application.command.DisputeSupplierInvoice;

public record DisputeSupplierInvoiceCommand(
        Long invoiceId,
        String reason
) {}
