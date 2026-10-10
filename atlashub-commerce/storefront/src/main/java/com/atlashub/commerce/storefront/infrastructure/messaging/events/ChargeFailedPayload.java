package com.atlashub.commerce.storefront.infrastructure.messaging.events;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ChargeFailedPayload(
        String eventId,
        String eventType,
        Payload payload
) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Payload(
            String chargeReference,
            String sourceReferenceId,
            String reason
    ) {
        public Long orderId() {
            return sourceReferenceId != null ? Long.parseLong(sourceReferenceId) : null;
        }
    }
}
