package com.atlashub.logistics.warehousing.application.command.RegisterWarehouse;

public record RegisterWarehouseResult(
        Long warehouseId,
        String name,
        String code,
        String facilityType,
        String status
) {}
