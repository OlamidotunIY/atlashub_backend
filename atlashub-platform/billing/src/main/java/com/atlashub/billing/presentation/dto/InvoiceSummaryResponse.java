package com.atlashub.billing.presentation.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZonedDateTime;

public record InvoiceSummaryResponse(
        Long id,
        Long organizationId,
        Long subscriptionId,
        String invoiceNumber,
        String productCode,
        LocalDate periodStart,
        LocalDate periodEnd,
        BigDecimal subtotal,
        BigDecimal tax,
        BigDecimal totalAmount,
        String currency,
        String status,
        LocalDate dueDate,
        ZonedDateTime issuedAt,
        ZonedDateTime paidAt
) {
}
