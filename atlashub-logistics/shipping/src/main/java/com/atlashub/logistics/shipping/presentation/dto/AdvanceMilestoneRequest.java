package com.atlashub.logistics.shipping.presentation.dto;

import com.atlashub.logistics.shipping.domain.valueobject.TrackingMilestone;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record AdvanceMilestoneRequest(
        @NotNull TrackingMilestone milestone,
        String locationName,
        String state,
        String description,
        BigDecimal latitude,
        BigDecimal longitude
) {}
