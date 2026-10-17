package com.atlashub.logistics.warehousing.application.command.ReleaseWarehouseStock;

public record ReleaseWarehouseStockCommand(
        Long organizationId,
        Long warehouseId,
        Long productId,
        int quantity,
        String correlationId
) {}
