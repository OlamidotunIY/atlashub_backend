package com.atlashub.analytics.infrastructure.messaging.events;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
@JsonIgnoreProperties(ignoreUnknown = true)
public record ShipmentStatusPayload(
        String eventId,
        Long organizationId,
        String environment,
        String month,
        boolean isDelivered,
        boolean isReturned,
        Integer deliveryMinutes
) {}
