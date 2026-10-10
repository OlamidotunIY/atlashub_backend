package com.atlashub.commerce.storefront.application.commands.CreateOnlineOrder;

import com.atlashub.commerce.storefront.application.commands.ProcessPosCheckout.OrderItemDto;
import com.atlashub.commerce.storefront.domain.valueobject.PaymentMethod;

import java.util.List;

public record CreateOnlineOrderCommand(
        Long organizationId,
        Long outletId,
        Long customerId,
        List<OrderItemDto> items,
        String deliveryAddress,
        PaymentMethod paymentMethod
) {
}
