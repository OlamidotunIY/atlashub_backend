package com.atlashub.logistics.warehousing.application.query.GetWarehouse;

import com.atlashub.logistics.warehousing.domain.valueobject.Address;
import java.time.ZonedDateTime;

public record WarehouseResult(
        Long id,
        Long organizationId,
        String name,
        String code,
        String facilityType,
        Address address,
        Long managerId,
        String status,
        ZonedDateTime createdAt
) {}
