package com.atlashub.billing.application.command.GenerateTransactionFeeInvoice;

public record GenerateTransactionFeeInvoiceCommand(
        Long organizationId,
        String month
) {
}
