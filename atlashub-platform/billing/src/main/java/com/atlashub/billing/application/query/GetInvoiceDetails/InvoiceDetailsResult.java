package com.atlashub.billing.application.query.GetInvoiceDetails;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.List;

public record InvoiceDetailsResult(
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
        ZonedDateTime paidAt,
        String paymentReference,
        String paymentCheckoutUrl,
        List<InvoiceLineItemResult> lineItems
) {
}
