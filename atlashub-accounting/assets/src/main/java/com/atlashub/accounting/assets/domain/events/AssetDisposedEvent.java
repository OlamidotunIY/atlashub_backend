package com.atlashub.accounting.assets.domain.events;

import com.atlashub.shared.domain.event.DomainEvent;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public record AssetDisposedEvent(
        String eventId,
        Long aggregateId,
        ZonedDateTime occurredAt,
        String correlationId,
        Payload payload
) implements DomainEvent<AssetDisposedEvent.Payload> {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Payload(
            Long assetId,
            Long organizationId,
            BigDecimal saleValue,
            BigDecimal currentBookValue,
            BigDecimal gainOrLoss,
            String currency,
            LocalDate disposalDate
    ) {}

    public static AssetDisposedEvent of(
            Long assetId,
            Long organizationId,
            BigDecimal saleValue,
            BigDecimal currentBookValue,
            BigDecimal gainOrLoss,
            String currency,
            LocalDate disposalDate
    ) {
        return new AssetDisposedEvent(
                UUID.randomUUID().toString(),
                assetId,
                ZonedDateTime.now(),
                UUID.randomUUID().toString(),
                new Payload(
                        assetId,
                        organizationId,
                        saleValue,
                        currentBookValue,
                        gainOrLoss,
                        currency,
                        disposalDate
                )
        );
    }
}
