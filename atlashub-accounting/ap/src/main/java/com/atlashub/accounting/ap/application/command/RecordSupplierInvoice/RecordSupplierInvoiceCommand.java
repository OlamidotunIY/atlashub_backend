package com.atlashub.accounting.ap.application.command.RecordSupplierInvoice;

import com.atlashub.shared.domain.valueobject.Money;

import java.time.LocalDate;

public record RecordSupplierInvoiceCommand(
        Long organizationId,
        Long supplierId,
        Long purchaseOrderId,
        String invoiceNumber,
        Money amount,
        LocalDate dueDate
) {}
