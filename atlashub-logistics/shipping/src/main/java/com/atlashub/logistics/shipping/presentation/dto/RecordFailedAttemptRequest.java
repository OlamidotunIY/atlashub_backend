package com.atlashub.logistics.shipping.presentation.dto;

import com.atlashub.logistics.shipping.domain.valueobject.DeliveryFaultType;

public record RecordFailedAttemptRequest(
        boolean markFailedFinal,
        DeliveryFaultType faultType,
        String reason
) {}
