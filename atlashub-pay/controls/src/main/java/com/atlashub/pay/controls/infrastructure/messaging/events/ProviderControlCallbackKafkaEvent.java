package com.atlashub.pay.controls.infrastructure.messaging.events;

public record ProviderControlCallbackKafkaEvent(
        String eventId,
        Long aggregateId,
        String eventType,
        Payload payload
) {
    public record Payload(
            Long decisionId,
            String providerReference,
            String status,
            String reason
    ) {}
}
