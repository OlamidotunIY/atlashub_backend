package com.atlashub.billing.application.command.GenerateSubscriptionInvoice;

import java.math.BigDecimal;

public record GenerateSubscriptionInvoiceResult(
        Long invoiceId,
        String invoiceNumber,
        BigDecimal totalAmount,
        String status
) {
}
