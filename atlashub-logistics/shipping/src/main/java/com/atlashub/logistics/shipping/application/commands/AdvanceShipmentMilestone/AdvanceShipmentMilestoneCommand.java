package com.atlashub.logistics.shipping.application.commands.AdvanceShipmentMilestone;

import com.atlashub.logistics.shipping.domain.valueobject.TrackingMilestone;
import java.math.BigDecimal;

public record AdvanceShipmentMilestoneCommand(
        Long shipmentId,
        TrackingMilestone milestone,
        String locationName,
        String state,
        String description,
        BigDecimal latitude,
        BigDecimal longitude,
        String correlationId
) {}
