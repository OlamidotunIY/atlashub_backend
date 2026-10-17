package com.atlashub.logistics.shipping.domain.events;

import com.atlashub.shared.domain.event.DomainEvent;
import com.atlashub.shared.domain.valueobject.Money;
import java.time.ZonedDateTime;
import java.util.UUID;

public record MerchantShippingIncentiveCreditedEvent(
        String eventId,
        Long aggregateId,
        ZonedDateTime occurredAt,
        String correlationId,
        Payload payload
) implements DomainEvent<MerchantShippingIncentiveCreditedEvent.Payload> {

    public record Payload(
            Long incentiveId,
            Long organizationId,
            String billingYearMonth,
            int eligibleShipmentCount,
            Money rebateAmount,
            ZonedDateTime creditedAt
    ) {}

    public static MerchantShippingIncentiveCreditedEvent of(Long aggregateId, String correlationId, Payload payload) {
        return new MerchantShippingIncentiveCreditedEvent(
                UUID.randomUUID().toString(),
                aggregateId,
                ZonedDateTime.now(),
                correlationId,
                payload
        );
    }
}
