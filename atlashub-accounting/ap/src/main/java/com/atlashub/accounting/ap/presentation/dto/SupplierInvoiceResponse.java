package com.atlashub.accounting.ap.presentation.dto;

import com.atlashub.accounting.ap.domain.valueobject.ApStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZonedDateTime;

public record SupplierInvoiceResponse(
        Long id,
        Long organizationId,
        Long supplierId,
        Long purchaseOrderId,
        String invoiceNumber,
        BigDecimal amount,
        String currency,
        LocalDate dueDate,
        ApStatus status,
        ZonedDateTime paidAt,
        ZonedDateTime createdAt
) {}
