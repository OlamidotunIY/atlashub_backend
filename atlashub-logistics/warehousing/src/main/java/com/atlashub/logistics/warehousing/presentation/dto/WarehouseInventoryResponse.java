package com.atlashub.logistics.warehousing.presentation.dto;

import java.time.ZonedDateTime;

public record WarehouseInventoryResponse(
        Long id,
        Long organizationId,
        Long warehouseId,
        Long productId,
        int quantity,
        int reservedQuantity,
        int availableQuantity,
        ZonedDateTime updatedAt
) {}
