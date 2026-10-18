package com.atlashub.logistics.warehousing.application.command.AdjustWarehouseStock;

public record AdjustWarehouseStockCommand(
        Long organizationId,
        Long warehouseId,
        Long productId,
        int newQuantity,
        String correlationId
) {}
