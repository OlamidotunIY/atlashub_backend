package com.atlashub.logistics.warehousing.presentation.dto;

import com.atlashub.logistics.warehousing.domain.valueobject.Address;
import com.atlashub.logistics.warehousing.domain.valueobject.FacilityType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record RegisterWarehouseRequest(
        @NotBlank(message = "Warehouse name is required")
        String name,

        @NotBlank(message = "Warehouse code is required")
        String code,

        @NotNull(message = "Facility type is required")
        FacilityType facilityType,

        @NotNull(message = "Address is required")
        Address address,

        Long managerId
) {}
