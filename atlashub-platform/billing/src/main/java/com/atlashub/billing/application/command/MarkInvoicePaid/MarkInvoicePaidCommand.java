package com.atlashub.billing.application.command.MarkInvoicePaid;

import java.time.ZonedDateTime;

public record MarkInvoicePaidCommand(
        Long invoiceId,
        String paymentReference,
        ZonedDateTime paidAt
) {
}
