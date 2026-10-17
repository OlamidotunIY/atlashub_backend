package com.atlashub.support.application.query.GetSlaMetrics;

import com.atlashub.support.domain.valueobject.TicketCategory;

public record SlaCategoryBreakdownResult(
        TicketCategory category,
        int total,
        int breached,
        double complianceRate
) {}
