package com.atlashub.billing.application.command.GenerateTransactionFeeInvoice;

import java.math.BigDecimal;

public record GenerateTransactionFeeInvoiceResult(
        Long invoiceId,
        String invoiceNumber,
        BigDecimal totalAmount,
        String status
) {
}
