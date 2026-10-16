package com.atlashub.pay.controls.infrastructure.messaging.events;

public record FinancingAnomalyKafkaEvent(
        String eventId,
        Long aggregateId,
        String eventType,
        Payload payload
) {
    public record Payload(
            Long facilityId,
            Long organizationId,
            String anomalyType,
            String severity,
            Long riskSignalId
    ) {}
}
