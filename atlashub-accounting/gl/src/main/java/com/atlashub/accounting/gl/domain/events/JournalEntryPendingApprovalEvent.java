package com.atlashub.accounting.gl.domain.events;

import com.atlashub.shared.domain.event.DomainEvent;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public record JournalEntryPendingApprovalEvent(
        String eventId,
        Long aggregateId,
        ZonedDateTime occurredAt,
        String correlationId,
        Payload payload
) implements DomainEvent<JournalEntryPendingApprovalEvent.Payload> {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Payload(
            Long entryId,
            Long organizationId,
            String entryNumber,
            BigDecimal amount,
            String currency,
            Long initiatedBy,
            ZonedDateTime submittedAt
    ) {
    }

    public static JournalEntryPendingApprovalEvent of(
            Long entryId,
            Long organizationId,
            String entryNumber,
            BigDecimal amount,
            String currency,
            Long initiatedBy
    ) {
        ZonedDateTime now = ZonedDateTime.now();
        return new JournalEntryPendingApprovalEvent(
                UUID.randomUUID().toString(),
                entryId,
                now,
                UUID.randomUUID().toString(),
                new Payload(
                        entryId,
                        organizationId,
                        entryNumber,
                        amount,
                        currency,
                        initiatedBy,
                        now
                )
        );
    }
}
