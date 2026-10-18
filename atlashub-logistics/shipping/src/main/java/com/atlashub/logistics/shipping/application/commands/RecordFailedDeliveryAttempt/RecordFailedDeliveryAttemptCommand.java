package com.atlashub.logistics.shipping.application.commands.RecordFailedDeliveryAttempt;

import com.atlashub.logistics.shipping.domain.valueobject.DeliveryFaultType;

public record RecordFailedDeliveryAttemptCommand(
        Long shipmentId,
        boolean markFailedFinal,
        DeliveryFaultType faultType,
        String reason,
        String correlationId
) {}
