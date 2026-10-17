package com.atlashub.analytics.application.query.GetDeliveryPerformance;

public record GetDeliveryPerformanceQuery(
        Long organizationId,
        String environment,
        String month
) {}
