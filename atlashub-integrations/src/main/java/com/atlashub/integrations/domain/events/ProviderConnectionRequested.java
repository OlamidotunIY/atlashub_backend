package com.atlashub.integrations.domain.events;

import com.atlashub.shared.domain.event.DomainEvent;
import java.time.ZonedDateTime;

public record ProviderConnectionRequested(String eventId, Long aggregateId, ZonedDateTime occurredAt,
                                          String correlationId, Payload payload)
        implements DomainEvent<ProviderConnectionRequested.Payload> {
    public record Payload(Long organizationId, String environment, String provider, String merchantCountry) {}
}
