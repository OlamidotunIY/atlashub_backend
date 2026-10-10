package com.atlashub.commerce.inventory.application.commands.ReserveStockForOrder;

import java.util.List;

public record ReserveStockForOrderCommand(
        Long salesOrderId,
        Long organizationId,
        Long outletId,
        List<OrderItemDto> items
) {
}
