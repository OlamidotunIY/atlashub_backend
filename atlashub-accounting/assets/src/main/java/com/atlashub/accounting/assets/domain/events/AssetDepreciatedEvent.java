package com.atlashub.accounting.assets.domain.events;

import com.atlashub.shared.domain.event.DomainEvent;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public record AssetDepreciatedEvent(
        String eventId,
        Long aggregateId,
        ZonedDateTime occurredAt,
        String correlationId,
        Payload payload
) implements DomainEvent<AssetDepreciatedEvent.Payload> {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Payload(
            Long assetId,
            Long organizationId,
            BigDecimal depreciationAmount,
            BigDecimal currentBookValue,
            BigDecimal accumulatedDepreciation,
            String currency,
            LocalDate asOfDate
    ) {}

    public static AssetDepreciatedEvent of(
            Long assetId,
            Long organizationId,
            BigDecimal depreciationAmount,
            BigDecimal currentBookValue,
            BigDecimal accumulatedDepreciation,
            String currency,
            LocalDate asOfDate
    ) {
        return new AssetDepreciatedEvent(
                UUID.randomUUID().toString(),
                assetId,
                ZonedDateTime.now(),
                UUID.randomUUID().toString(),
                new Payload(
                        assetId,
                        organizationId,
                        depreciationAmount,
                        currentBookValue,
                        accumulatedDepreciation,
                        currency,
                        asOfDate
                )
        );
    }
}
