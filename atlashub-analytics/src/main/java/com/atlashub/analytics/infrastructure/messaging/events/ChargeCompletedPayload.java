package com.atlashub.analytics.infrastructure.messaging.events;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.math.BigDecimal;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ChargeCompletedPayload(
        String eventId,
        Long organizationId,
        String environment,
        String month,
        BigDecimal amount,
        BigDecimal platformFee,
        boolean successful
) {}
