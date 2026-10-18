package com.atlashub.analytics.application.query.GetSalesSummary;

import java.time.LocalDate;

public record GetSalesSummaryQuery(
        Long organizationId,
        String environment,
        Long outletId,
        LocalDate from,
        LocalDate to
) {}
