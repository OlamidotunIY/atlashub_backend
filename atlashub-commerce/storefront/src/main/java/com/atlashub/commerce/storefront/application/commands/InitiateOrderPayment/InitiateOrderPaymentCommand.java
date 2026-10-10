package com.atlashub.commerce.storefront.application.commands.InitiateOrderPayment;

public record InitiateOrderPaymentCommand(
        Long salesOrderId,
        String chargeReference
) {
}
