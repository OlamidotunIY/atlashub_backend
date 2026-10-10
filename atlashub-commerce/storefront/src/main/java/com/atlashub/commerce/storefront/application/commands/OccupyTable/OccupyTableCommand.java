package com.atlashub.commerce.storefront.application.commands.OccupyTable;

public record OccupyTableCommand(
        Long tableId,
        Long salesOrderId,
        int covers
) {
}
