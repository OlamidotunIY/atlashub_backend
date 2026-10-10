package com.atlashub.pay.charges.domain.events;

import com.atlashub.shared.domain.event.DomainEvent;
import com.atlashub.shared.domain.valueobject.Money;

import java.time.ZonedDateTime;

public record ChargeSuccessfulEvent(String eventId, Long aggregateId, ZonedDateTime occurredAt, String correlationId,
                                    Payload payload) implements DomainEvent<ChargeSuccessfulEvent.Payload> {
    public record Payload(Long organizationId, String environment, String chargeReference, String gatewayReference,
                          Money amount, Money providerFee, String channel, String provider, String sourceSystem,
                          String sourceReferenceId, String customerReferenceId, ZonedDateTime succeededAt) {
    }
}
