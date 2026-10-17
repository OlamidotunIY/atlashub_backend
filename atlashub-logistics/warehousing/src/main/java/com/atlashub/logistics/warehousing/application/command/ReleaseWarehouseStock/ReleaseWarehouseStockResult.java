package com.atlashub.logistics.warehousing.application.command.ReleaseWarehouseStock;

public record ReleaseWarehouseStockResult(
        Long inventoryId,
        int quantity,
        int reservedQuantity,
        int availableQuantity
) {}
