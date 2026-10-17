package com.atlashub.support.presentation.dto;

import java.util.List;

public record SlaMetricsResponse(
        int totalTickets,
        double firstResponseSlaRate,
        double resolutionSlaRate,
        double avgFirstResponseMinutes,
        double avgResolutionMinutes,
        int breachedCount,
        List<SlaCategoryBreakdownResponse> byCategory
) {}
