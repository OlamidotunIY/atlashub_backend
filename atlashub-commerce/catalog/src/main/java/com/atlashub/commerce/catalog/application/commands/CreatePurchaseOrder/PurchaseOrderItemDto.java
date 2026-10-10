package com.atlashub.commerce.catalog.application.commands.CreatePurchaseOrder;

import com.atlashub.shared.domain.valueobject.Money;

public record PurchaseOrderItemDto(
        Long productId,
        int quantity,
        Money unitCost
) {
}
