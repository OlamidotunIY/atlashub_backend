package com.atlashub.pay.charges.domain.events;

import com.atlashub.shared.domain.event.DomainEvent;
import com.atlashub.shared.domain.valueobject.Money;

import java.time.ZonedDateTime;

public record ChargeRefundedEvent(
        String eventId,
        Long aggregateId,
        ZonedDateTime occurredAt,
        String correlationId,
        Payload payload
) implements DomainEvent<ChargeRefundedEvent.Payload> {
    public record Payload(Long organizationId, String environment, String chargeReference,
                          String providerRefundReference, Money amount, String sourceSystem,
                          String sourceReferenceId, String customerReferenceId, ZonedDateTime refundedAt) {
    }
}
