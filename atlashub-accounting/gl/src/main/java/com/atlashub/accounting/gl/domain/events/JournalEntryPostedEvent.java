package com.atlashub.accounting.gl.domain.events;

import com.atlashub.shared.domain.event.DomainEvent;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public record JournalEntryPostedEvent(
        String eventId,
        Long aggregateId,
        ZonedDateTime occurredAt,
        String correlationId,
        Payload payload
) implements DomainEvent<JournalEntryPostedEvent.Payload> {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Payload(
            Long entryId,
            Long organizationId,
            String entryNumber,
            String reference,
            BigDecimal totalAmount,
            String currency,
            ZonedDateTime postedAt
    ) {
    }

    public static JournalEntryPostedEvent of(
            Long entryId,
            Long organizationId,
            String entryNumber,
            String reference,
            BigDecimal totalAmount,
            String currency
    ) {
        ZonedDateTime now = ZonedDateTime.now();
        return new JournalEntryPostedEvent(
                UUID.randomUUID().toString(),
                entryId,
                now,
                UUID.randomUUID().toString(),
                new Payload(
                        entryId,
                        organizationId,
                        entryNumber,
                        reference,
                        totalAmount,
                        currency,
                        now
                )
        );
    }
}
