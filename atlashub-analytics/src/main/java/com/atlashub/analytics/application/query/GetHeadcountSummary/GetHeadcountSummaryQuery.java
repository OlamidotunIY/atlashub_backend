package com.atlashub.analytics.application.query.GetHeadcountSummary;

public record GetHeadcountSummaryQuery(
        Long organizationId,
        String environment
) {}
