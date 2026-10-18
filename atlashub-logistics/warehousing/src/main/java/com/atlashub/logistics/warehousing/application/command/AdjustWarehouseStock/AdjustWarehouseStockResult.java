package com.atlashub.logistics.warehousing.application.command.AdjustWarehouseStock;

public record AdjustWarehouseStockResult(
        Long inventoryId,
        Long warehouseId,
        Long productId,
        int quantity,
        int reservedQuantity,
        int availableQuantity
) {}
