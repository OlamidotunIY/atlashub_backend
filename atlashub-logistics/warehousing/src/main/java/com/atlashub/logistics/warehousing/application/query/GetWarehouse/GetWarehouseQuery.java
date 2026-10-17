package com.atlashub.logistics.warehousing.application.query.GetWarehouse;

public record GetWarehouseQuery(
        Long organizationId,
        Long warehouseId
) {}
