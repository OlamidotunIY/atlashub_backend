package com.atlashub.analytics.domain.events;

import com.atlashub.analytics.domain.valueobject.DataQualityStatus;
import com.atlashub.shared.domain.event.DomainEvent;
import java.time.ZonedDateTime;
import java.util.UUID;

public record AnalyticsDataQualityChangedEvent(
        String eventId,
        Long aggregateId,
        ZonedDateTime occurredAt,
        String correlationId,
        Payload payload
) implements DomainEvent<AnalyticsDataQualityChangedEvent.Payload> {

    public record Payload(
            Long organizationId,
            String environment,
            String metricStream,
            DataQualityStatus previousStatus,
            DataQualityStatus newStatus,
            String reason
    ) {}

    public static AnalyticsDataQualityChangedEvent of(Long aggregateId, String correlationId, Payload payload) {
        return new AnalyticsDataQualityChangedEvent(
                UUID.randomUUID().toString(),
                aggregateId,
                ZonedDateTime.now(),
                correlationId,
                payload
        );
    }
}
