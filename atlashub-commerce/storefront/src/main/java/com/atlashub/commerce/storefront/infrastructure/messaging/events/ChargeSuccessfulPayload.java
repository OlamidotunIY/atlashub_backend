package com.atlashub.commerce.storefront.infrastructure.messaging.events;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ChargeSuccessfulPayload(
        String eventId,
        String eventType,
        Payload payload
) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Payload(
            String chargeReference,
            String sourceReferenceId
    ) {
        public Long orderId() {
            return sourceReferenceId != null ? Long.parseLong(sourceReferenceId) : null;
        }
    }
}
