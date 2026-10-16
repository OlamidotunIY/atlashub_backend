package com.atlashub.pay.mandates.domain.events;

import com.atlashub.shared.domain.event.DomainEvent;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public record MandateChargeDueEvent(
        String eventId,
        Long aggregateId,
        ZonedDateTime occurredAt,
        String correlationId,
        Payload payload
) implements DomainEvent<MandateChargeDueEvent.Payload> {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Payload(
            Long mandateId,
            Long organizationId,
            String customerId,
            String email,
            String chargeReference,
            BigDecimal amount,
            String currency,
            String authorizationCode,
            String paymentChannel,
            String provider,
            String sourceReferenceId,
            ZonedDateTime dueAt
    ) {}

    public static MandateChargeDueEvent of(
            Long mandateId,
            Long organizationId,
            String customerId,
            String email,
            String chargeReference,
            BigDecimal amount,
            String currency,
            String authorizationCode,
            ZonedDateTime dueAt
    ) {
        return new MandateChargeDueEvent(
                UUID.randomUUID().toString(),
                mandateId,
                ZonedDateTime.now(),
                UUID.randomUUID().toString(),
                new Payload(
                        mandateId,
                        organizationId,
                        customerId,
                        email,
                        chargeReference,
                        amount,
                        currency,
                        authorizationCode,
                        "CARD",
                        "PAYSTACK",
                        String.valueOf(mandateId),
                        dueAt
                )
        );
    }
}
