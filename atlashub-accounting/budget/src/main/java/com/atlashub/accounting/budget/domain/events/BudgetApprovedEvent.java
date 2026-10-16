package com.atlashub.accounting.budget.domain.events;

import com.atlashub.shared.domain.event.DomainEvent;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public record BudgetApprovedEvent(
        String eventId,
        Long aggregateId,
        ZonedDateTime occurredAt,
        String correlationId,
        Payload payload
) implements DomainEvent<BudgetApprovedEvent.Payload> {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Payload(
            Long budgetId,
            Long organizationId,
            Long accountId,
            String name,
            String period,
            BigDecimal budgetedAmount,
            String currency,
            Long approvedBy,
            ZonedDateTime approvedAt
    ) {}

    public static BudgetApprovedEvent of(
            Long budgetId,
            Long organizationId,
            Long accountId,
            String name,
            String period,
            BigDecimal budgetedAmount,
            String currency,
            Long approvedBy,
            ZonedDateTime approvedAt
    ) {
        return new BudgetApprovedEvent(
                UUID.randomUUID().toString(),
                budgetId,
                ZonedDateTime.now(),
                UUID.randomUUID().toString(),
                new Payload(
                        budgetId,
                        organizationId,
                        accountId,
                        name,
                        period,
                        budgetedAmount,
                        currency,
                        approvedBy,
                        approvedAt
                )
        );
    }
}
