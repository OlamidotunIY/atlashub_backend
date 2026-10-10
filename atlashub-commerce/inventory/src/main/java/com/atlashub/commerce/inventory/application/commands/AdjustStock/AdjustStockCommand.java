package com.atlashub.commerce.inventory.application.commands.AdjustStock;

import com.atlashub.commerce.inventory.domain.valueobject.AdjustmentReason;

public record AdjustStockCommand(
        Long inventoryId,
        int newQuantity,
        AdjustmentReason reason,
        Long adjustedBy
) {
}
