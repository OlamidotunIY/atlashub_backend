package com.atlashub.support.presentation.dto;

import com.atlashub.support.domain.valueobject.TicketCategory;

public record SlaCategoryBreakdownResponse(
        TicketCategory category,
        int total,
        int breached,
        double complianceRate
) {}
