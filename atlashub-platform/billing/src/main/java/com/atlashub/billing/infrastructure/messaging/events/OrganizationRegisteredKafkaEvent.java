package com.atlashub.billing.infrastructure.messaging.events;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record OrganizationRegisteredKafkaEvent(
        String eventId,
        Long aggregateId,
        Payload payload
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Payload(
            Long ownerUserId,
            String currency,
            String businessName
    ) {
    }
}
