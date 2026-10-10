package com.atlashub.commerce.inventory.application.commands.ReconcileStockCount;

public record CountedItemDto(
        Long inventoryId,
        int countedQty
) {
}
