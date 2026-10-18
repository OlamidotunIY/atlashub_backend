package com.atlashub.analytics.domain.events;

import com.atlashub.analytics.domain.valueobject.DataQualityStatus;
import com.atlashub.shared.domain.event.DomainEvent;
import java.time.ZonedDateTime;
import java.util.UUID;

public record AnalyticsFeatureSnapshotReadyEvent(
        String eventId,
        Long aggregateId,
        ZonedDateTime occurredAt,
        String correlationId,
        Payload payload
) implements DomainEvent<AnalyticsFeatureSnapshotReadyEvent.Payload> {

    public record Payload(
            Long snapshotId,
            Long organizationId,
            String environment,
            String schemaVersion,
            ZonedDateTime asOf,
            DataQualityStatus completeness
    ) {}

    public static AnalyticsFeatureSnapshotReadyEvent of(Long aggregateId, String correlationId, Payload payload) {
        return new AnalyticsFeatureSnapshotReadyEvent(
                UUID.randomUUID().toString(),
                aggregateId,
                ZonedDateTime.now(),
                correlationId,
                payload
        );
    }
}
