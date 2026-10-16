package com.atlashub.billing.presentation.dto;

import java.math.BigDecimal;

public record InvoiceLineItemResponse(
        Long id,
        Long invoiceId,
        String description,
        Integer quantity,
        BigDecimal unitPrice,
        BigDecimal totalAmount,
        String lineItemType
) {
}
