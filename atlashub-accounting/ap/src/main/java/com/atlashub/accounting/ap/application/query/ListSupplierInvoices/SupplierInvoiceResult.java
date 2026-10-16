package com.atlashub.accounting.ap.application.query.ListSupplierInvoices;

import com.atlashub.accounting.ap.domain.valueobject.ApStatus;
import com.atlashub.shared.domain.valueobject.Money;

import java.time.LocalDate;
import java.time.ZonedDateTime;

public record SupplierInvoiceResult(
        Long id,
        Long organizationId,
        Long supplierId,
        Long purchaseOrderId,
        String invoiceNumber,
        Money amount,
        LocalDate dueDate,
        ApStatus status,
        ZonedDateTime paidAt,
        ZonedDateTime createdAt
) {}
