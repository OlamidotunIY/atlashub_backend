package com.atlashub.pay.mandates.domain.events;

import com.atlashub.shared.domain.event.DomainEvent;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public record MandateChargedEvent(
        String eventId,
        Long aggregateId,
        ZonedDateTime occurredAt,
        String correlationId,
        Payload payload
) implements DomainEvent<MandateChargedEvent.Payload> {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Payload(
            Long mandateId,
            Long organizationId,
            String customerId,
            String email,
            BigDecimal amount,
            String currency,
            String chargeReference,
            ZonedDateTime chargedAt
    ) {}

    public static MandateChargedEvent of(
            Long mandateId,
            Long organizationId,
            String customerId,
            String email,
            BigDecimal amount,
            String currency,
            String chargeReference,
            ZonedDateTime chargedAt
    ) {
        return new MandateChargedEvent(
                UUID.randomUUID().toString(),
                mandateId,
                ZonedDateTime.now(),
                UUID.randomUUID().toString(),
                new Payload(
                        mandateId,
                        organizationId,
                        customerId,
                        email,
                        amount,
                        currency,
                        chargeReference,
                        chargedAt
                )
        );
    }
}
