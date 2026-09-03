package com.atlashub.billing.application.command;

import com.atlashub.billing.domain.valueobject.PaymentMethod;

public record CheckoutSubscriptionCommand(
        Long organizationId,
        Long productId,
        PaymentMethod paymentMethod
) {}