package com.atlashub.billing.adapter.in.web.request;

import com.atlashub.billing.domain.valueobject.PaymentMethod;
import jakarta.validation.constraints.NotNull;

public record CheckoutSubscriptionRequest(
        @NotNull(message = "productId is required") Long productId,
        @NotNull(message = "paymentMethod is required") PaymentMethod paymentMethod
) {}
