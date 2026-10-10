package com.atlashub.commerce.inventory.application.commands.DeductReservedStock;

public record DeductReservedStockCommand(
        Long salesOrderId
) {
}
