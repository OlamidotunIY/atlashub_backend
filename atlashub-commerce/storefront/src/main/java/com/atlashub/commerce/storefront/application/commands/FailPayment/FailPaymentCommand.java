package com.atlashub.commerce.storefront.application.commands.FailPayment;

public record FailPaymentCommand(
        Long salesOrderId,
        String reason
) {
}
