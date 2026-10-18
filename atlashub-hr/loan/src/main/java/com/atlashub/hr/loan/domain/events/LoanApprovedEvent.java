package com.atlashub.hr.loan.domain.events;

import com.atlashub.shared.domain.event.DomainEvent;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public record LoanApprovedEvent(
        String eventId,
        Long aggregateId,
        ZonedDateTime occurredAt,
        String correlationId,
        Payload payload
) implements DomainEvent<LoanApprovedEvent.Payload> {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Payload(
            Long loanId,
            Long employeeId,
            Long organizationId,
            BigDecimal principalAmount,
            String currency,
            BigDecimal monthlyDeduction,
            LocalDate startDate,
            Long approvedBy
    ) {}

    public static LoanApprovedEvent of(
            Long loanId,
            Long employeeId,
            Long organizationId,
            BigDecimal principalAmount,
            String currency,
            BigDecimal monthlyDeduction,
            LocalDate startDate,
            Long approvedBy
    ) {
        return new LoanApprovedEvent(
                UUID.randomUUID().toString(),
                loanId,
                ZonedDateTime.now(),
                UUID.randomUUID().toString(),
                new Payload(
                        loanId,
                        employeeId,
                        organizationId,
                        principalAmount,
                        currency,
                        monthlyDeduction,
                        startDate,
                        approvedBy
                )
        );
    }
}
