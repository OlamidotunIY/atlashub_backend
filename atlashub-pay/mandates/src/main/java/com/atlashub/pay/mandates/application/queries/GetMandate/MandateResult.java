package com.atlashub.pay.mandates.application.queries.GetMandate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZonedDateTime;

public record MandateResult(
        Long id,
        Long organizationId,
        String customerId,
        String email,
        BigDecimal amount,
        String currency,
        String frequency,
        String status,
        LocalDate nextChargeDate,
        ZonedDateTime createdAt,
        ZonedDateTime updatedAt
) {}
