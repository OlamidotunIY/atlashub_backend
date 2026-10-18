package com.atlashub.logistics.warehousing.application.command.ReserveWarehouseStock;

public record ReserveWarehouseStockResult(
        Long inventoryId,
        int quantity,
        int reservedQuantity,
        int availableQuantity
) {}
