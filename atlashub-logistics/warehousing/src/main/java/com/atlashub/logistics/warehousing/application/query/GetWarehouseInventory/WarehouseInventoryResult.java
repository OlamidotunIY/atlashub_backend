package com.atlashub.logistics.warehousing.application.query.GetWarehouseInventory;

import java.time.ZonedDateTime;

public record WarehouseInventoryResult(
        Long id,
        Long organizationId,
        Long warehouseId,
        Long productId,
        int quantity,
        int reservedQuantity,
        int availableQuantity,
        ZonedDateTime updatedAt
) {}
