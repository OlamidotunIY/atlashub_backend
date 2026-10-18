package com.atlashub.support.application.query.GetSlaMetrics;

import java.time.ZonedDateTime;

public record GetSlaMetricsQuery(
        ZonedDateTime dateFrom,
        ZonedDateTime dateTo
) {}
