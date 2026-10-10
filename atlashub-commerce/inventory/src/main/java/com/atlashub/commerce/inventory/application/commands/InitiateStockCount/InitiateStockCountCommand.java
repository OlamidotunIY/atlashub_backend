package com.atlashub.commerce.inventory.application.commands.InitiateStockCount;

public record InitiateStockCountCommand(
        Long organizationId,
        Long outletId,
        Long initiatedBy
) {
}
