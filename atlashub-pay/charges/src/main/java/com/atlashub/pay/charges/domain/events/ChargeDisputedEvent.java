package com.atlashub.pay.charges.domain.events;

import com.atlashub.shared.domain.event.DomainEvent;
import com.atlashub.shared.domain.valueobject.Money;
import java.time.ZonedDateTime;

public record ChargeDisputedEvent(String eventId,Long aggregateId,ZonedDateTime occurredAt,String correlationId,
                                  Payload payload) implements DomainEvent<ChargeDisputedEvent.Payload> {
    public record Payload(Long organizationId,String environment,String chargeReference,String disputeReference,
                          Money amount,String reason,ZonedDateTime disputedAt){}
}
