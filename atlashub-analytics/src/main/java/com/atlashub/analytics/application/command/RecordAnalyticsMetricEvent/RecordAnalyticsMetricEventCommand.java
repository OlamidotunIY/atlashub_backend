package com.atlashub.analytics.application.command.RecordAnalyticsMetricEvent;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

public record RecordAnalyticsMetricEventCommand(
        String eventId,
        Long organizationId,
        String environment,
        String metricType,
        String module,
        BigDecimal amount,
        String currency,
        String metadata,
        ZonedDateTime occurredAt
) {}
