package com.atlashub.identity.application.command;

import com.atlashub.identity.domain.valueobject.StaffSize;
import java.math.BigDecimal;

public record CompleteComplianceProfileCommand(
    Long OrganizationId,
    String description,
    StaffSize staffSize,
    String industry,
    String category,
    BigDecimal annualProjectedSalesVolume,
    String annualProjectedSalesCurrency
) {}

