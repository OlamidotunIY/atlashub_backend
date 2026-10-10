package com.atlashub.pay.charges.domain.events;

import com.atlashub.shared.domain.event.DomainEvent;
import java.time.ZonedDateTime;

public record ChargeDisputeResolvedEvent(String eventId,Long aggregateId,ZonedDateTime occurredAt,String correlationId,
                                         Payload payload) implements DomainEvent<ChargeDisputeResolvedEvent.Payload> {
    public record Payload(Long organizationId,String environment,String chargeReference,String disputeReference,
                          String sourceSystem,String sourceReferenceId,String customerReferenceId,
                          String resolution,ZonedDateTime resolvedAt){}
}
