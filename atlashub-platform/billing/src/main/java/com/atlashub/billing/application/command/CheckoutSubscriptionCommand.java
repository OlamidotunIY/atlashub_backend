package com.atlashub.billing.application.command;

public record CheckoutSubscriptionCommand(
        Long organizationId,
        Long productId,
        String paymentMethod // "WALLET" or "CHARGE"
) {}