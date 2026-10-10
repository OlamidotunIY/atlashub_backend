package com.atlashub.commerce.storefront.application.commands.SendKitchenOrder;

import java.util.List;

public record SendKitchenOrderCommand(
        Long salesOrderId,
        Long tableId,
        Long outletId,
        List<KotItemDto> items
) {
}
