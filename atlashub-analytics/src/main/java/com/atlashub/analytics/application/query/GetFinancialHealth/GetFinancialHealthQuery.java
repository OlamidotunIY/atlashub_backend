package com.atlashub.analytics.application.query.GetFinancialHealth;

import java.time.LocalDate;

public record GetFinancialHealthQuery(
        Long organizationId,
        String environment,
        LocalDate periodEnd
) {}
