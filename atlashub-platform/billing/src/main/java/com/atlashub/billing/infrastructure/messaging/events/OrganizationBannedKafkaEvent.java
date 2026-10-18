package com.atlashub.billing.infrastructure.messaging.events;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record OrganizationBannedKafkaEvent(
        String eventId,
        Long aggregateId,
        Payload payload
) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Payload(
            Long organizationId,
            String reason
    ) {
    }
}
