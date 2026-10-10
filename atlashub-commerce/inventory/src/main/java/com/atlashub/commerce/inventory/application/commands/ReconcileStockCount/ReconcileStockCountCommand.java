package com.atlashub.commerce.inventory.application.commands.ReconcileStockCount;

import java.util.List;

public record ReconcileStockCountCommand(
        Long stockCountId,
        List<CountedItemDto> countedItems,
        Long reconciledBy
) {
}
