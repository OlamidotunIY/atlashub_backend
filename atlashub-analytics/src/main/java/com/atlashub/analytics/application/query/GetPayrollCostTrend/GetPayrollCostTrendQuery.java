package com.atlashub.analytics.application.query.GetPayrollCostTrend;

public record GetPayrollCostTrendQuery(
        Long organizationId,
        String environment,
        String year
) {}
