package com.atlashub.analytics.application.query.GetBusinessPerformance;

import java.time.LocalDate;

public record GetBusinessPerformanceQuery(
        Long organizationId,
        String environment,
        LocalDate asOfDate
) {}
