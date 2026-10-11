package com.atlashub.accounting.cash.domain.events;

import com.atlashub.shared.domain.event.DomainEvent;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public record BankReconciliationCompletedEvent(
        String eventId,
        Long aggregateId,
        ZonedDateTime occurredAt,
        String correlationId,
        Payload payload
) implements DomainEvent<BankReconciliationCompletedEvent.Payload> {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Payload(
            Long reconciliationId,
            Long organizationId,
            String bankAccountCode,
            LocalDate statementDate,
            BigDecimal statementClosingBalance,
            BigDecimal bookBalance,
            BigDecimal difference,
            String currency,
            ZonedDateTime reconciledAt
    ) {}

    public static BankReconciliationCompletedEvent of(
            Long reconciliationId,
            Long organizationId,
            String bankAccountCode,
            LocalDate statementDate,
            BigDecimal statementClosingBalance,
            BigDecimal bookBalance,
            BigDecimal difference,
            String currency,
            ZonedDateTime reconciledAt
    ) {
        ZonedDateTime now = ZonedDateTime.now();
        return new BankReconciliationCompletedEvent(
                UUID.randomUUID().toString(),
                reconciliationId,
                now,
                UUID.randomUUID().toString(),
                new Payload(
                        reconciliationId,
                        organizationId,
                        bankAccountCode,
                        statementDate,
                        statementClosingBalance,
                        bookBalance,
                        difference,
                        currency,
                        reconciledAt != null ? reconciledAt : now
                )
        );
    }
}
