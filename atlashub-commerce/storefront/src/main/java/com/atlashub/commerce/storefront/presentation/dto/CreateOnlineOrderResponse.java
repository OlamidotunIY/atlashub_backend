package com.atlashub.commerce.storefront.presentation.dto;

import com.atlashub.commerce.storefront.domain.valueobject.OrderStatus;

public record CreateOnlineOrderResponse(
        Long salesOrderId,
        OrderStatus status
) {
}
