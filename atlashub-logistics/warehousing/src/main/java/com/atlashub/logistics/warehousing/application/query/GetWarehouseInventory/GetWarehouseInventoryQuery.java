package com.atlashub.logistics.warehousing.application.query.GetWarehouseInventory;

public record GetWarehouseInventoryQuery(
        Long organizationId,
        Long warehouseId
) {}
