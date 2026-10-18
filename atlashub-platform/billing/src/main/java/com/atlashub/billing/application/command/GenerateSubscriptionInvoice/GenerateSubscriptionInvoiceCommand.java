package com.atlashub.billing.application.command.GenerateSubscriptionInvoice;

import java.math.BigDecimal;
import java.time.LocalDate;

public record GenerateSubscriptionInvoiceCommand(
        Long subscriptionId,
        LocalDate periodStart,
        LocalDate periodEnd,
        BigDecimal amount
) {
}
