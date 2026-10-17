package com.atlashub.analytics.application.query.GetOutletPerformance;

public record GetOutletPerformanceQuery(
        Long organizationId,
        String environment,
        String month
) {}
