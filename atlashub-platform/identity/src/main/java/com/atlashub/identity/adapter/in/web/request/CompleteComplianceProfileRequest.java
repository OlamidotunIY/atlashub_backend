package com.atlashub.identity.adapter.in.web.request;

import com.atlashub.identity.domain.valueobject.StaffSize;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record CompleteComplianceProfileRequest(
    String description,
    
    @NotNull(message = "Staff size is required")
    StaffSize staffSize,
    
    @NotBlank(message = "Industry is required")
    String industry,
    
    @NotBlank(message = "Category is required")
    String category,
    
    AnnualProjectedSalesVolumeDto annualProjectedSalesVolume
) {}

