package com.atlashub.logistics.warehousing.application.command.ReserveWarehouseStock;

public record ReserveWarehouseStockCommand(
        Long organizationId,
        Long warehouseId,
        Long productId,
        int quantity,
        String correlationId
) {}
