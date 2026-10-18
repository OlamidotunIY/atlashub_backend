package com.atlashub.analytics.infrastructure.messaging.events;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.math.BigDecimal;

@JsonIgnoreProperties(ignoreUnknown = true)
public record PayrollDisbursedPayload(
        String eventId,
        Long organizationId,
        String environment,
        String month,
        BigDecimal grossPayroll,
        BigDecimal deductions,
        BigDecimal netPayroll,
        int headcount
) {}
