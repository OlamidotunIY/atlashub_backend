package com.atlashub.commerce.storefront.application.commands.ProcessPosCheckout;

import com.atlashub.commerce.storefront.domain.valueobject.OrderStatus;

public record ProcessPosCheckoutResult(
        Long salesOrderId,
        OrderStatus status
) {
}
