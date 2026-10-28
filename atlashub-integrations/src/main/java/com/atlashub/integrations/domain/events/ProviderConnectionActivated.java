package com.atlashub.integrations.domain.events;

import com.atlashub.shared.domain.event.DomainEvent;
import java.time.ZonedDateTime;
import java.util.Set;

public record ProviderConnectionActivated(String eventId, Long aggregateId, ZonedDateTime occurredAt,
                                          String correlationId, Payload payload)
        implements DomainEvent<ProviderConnectionActivated.Payload> {
    public record Payload(Long organizationId, String environment, String provider, String merchantCountry,
                          String externalMerchantId, Set<String> activeCapabilities) {
        public Payload { activeCapabilities = Set.copyOf(activeCapabilities); }
    }
}
