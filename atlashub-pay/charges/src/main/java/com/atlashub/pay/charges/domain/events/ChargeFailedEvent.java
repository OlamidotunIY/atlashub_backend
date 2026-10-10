package com.atlashub.pay.charges.domain.events;

import com.atlashub.shared.domain.event.DomainEvent;
import com.atlashub.shared.domain.valueobject.Money;

import java.time.ZonedDateTime;

public record ChargeFailedEvent(String eventId, Long aggregateId, ZonedDateTime occurredAt, String correlationId,
                                Payload payload) implements DomainEvent<ChargeFailedEvent.Payload> {
    public record Payload(Long organizationId, String environment, String chargeReference, Money amount,
                          String channel, String provider, String sourceSystem, String sourceReferenceId,
                          String customerReferenceId, String reason, ZonedDateTime failedAt) {
    }
}
