package com.atlashub.logistics.warehousing.application.command.RegisterWarehouse;

import com.atlashub.logistics.warehousing.domain.valueobject.Address;
import com.atlashub.logistics.warehousing.domain.valueobject.FacilityType;

public record RegisterWarehouseCommand(
        Long organizationId,
        String name,
        String code,
        FacilityType facilityType,
        Address address,
        Long managerId,
        String correlationId
) {}
