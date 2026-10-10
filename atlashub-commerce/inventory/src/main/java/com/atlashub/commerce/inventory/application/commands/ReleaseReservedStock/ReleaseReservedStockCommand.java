package com.atlashub.commerce.inventory.application.commands.ReleaseReservedStock;

public record ReleaseReservedStockCommand(
        Long salesOrderId
) {
}
