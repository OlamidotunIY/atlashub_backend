package com.atlashub.commerce.storefront.application.commands.ProcessPosCheckout;

import com.atlashub.commerce.storefront.domain.valueobject.OrderType;
import com.atlashub.commerce.storefront.domain.valueobject.PaymentMethod;

import java.util.List;

public record ProcessPosCheckoutCommand(
        Long organizationId,
        Long outletId,
        Long tillId,
        Long customerId,
        OrderType type,
        List<OrderItemDto> items,
        Long discountId,
        PaymentMethod paymentMethod,
        Long cashierId
) {
}
