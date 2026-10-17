package com.atlashub.support.application.query.GetSlaMetrics;

import java.util.List;

public record SlaMetricsResult(
        int totalTickets,
        double firstResponseSlaRate,
        double resolutionSlaRate,
        double avgFirstResponseMinutes,
        double avgResolutionMinutes,
        int breachedCount,
        List<SlaCategoryBreakdownResult> byCategory
) {}
