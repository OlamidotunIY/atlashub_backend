package com.atlashub.analytics.infrastructure.messaging.events;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.atlashub.shared.domain.event.DomainEvent;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record LedgerTransactionPostedEvent(
        String eventId,
        Long aggregateId,
        ZonedDateTime occurredAt,
        String correlationId,
        Payload payload
) implements DomainEvent<LedgerTransactionPostedEvent.Payload> {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Payload(
            Long transactionId,
            Long organizationId,
            String environment,
            String reference,
            String sourceSystem,
            String sourceReferenceId,
            String description,
            String currency,
            ZonedDateTime postedAt,
            List<EntryPayload> entries
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record EntryPayload(Long accountId, String entryType, BigDecimal amount) {
    }
}
