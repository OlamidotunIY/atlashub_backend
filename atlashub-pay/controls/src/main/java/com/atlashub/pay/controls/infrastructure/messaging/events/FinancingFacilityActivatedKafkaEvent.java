package com.atlashub.pay.controls.infrastructure.messaging.events;

public record FinancingFacilityActivatedKafkaEvent(
        String eventId,
        Long aggregateId,
        String eventType,
        Payload payload
) {
    public record Payload(
            Long facilityId,
            Long organizationId,
            String policyVersion,
            String mode,
            String activatedBy
    ) {}
}
