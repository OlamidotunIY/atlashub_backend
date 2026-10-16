package com.atlashub.billing.application.query.GetInvoiceDetails;

import java.math.BigDecimal;

public record InvoiceLineItemResult(
        Long id,
        Long invoiceId,
        String description,
        Integer quantity,
        BigDecimal unitPrice,
        BigDecimal totalAmount,
        String lineItemType
) {
}
