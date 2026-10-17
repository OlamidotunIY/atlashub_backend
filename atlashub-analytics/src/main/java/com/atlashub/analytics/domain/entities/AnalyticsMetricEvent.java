package com.atlashub.analytics.domain.entities;

import com.atlashub.analytics.domain.exceptions.AnalyticsInvariantException;
import com.atlashub.shared.domain.entities.AggregateRoot;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

@Getter
public class AnalyticsMetricEvent extends AggregateRoot<Long> {

    private final Long id;
    private final String eventId;
    private final Long organizationId;
    private final String environment;
    private final String metricType;
    private final String module;
    private final BigDecimal amount;
    private final String currency;
    private final String metadata;
    private final ZonedDateTime occurredAt;

    public AnalyticsMetricEvent(
            Long id,
            String eventId,
            Long organizationId,
            String environment,
            String metricType,
            String module,
            BigDecimal amount,
            String currency,
            String metadata,
            ZonedDateTime occurredAt
    ) {
        this.id = id;
        this.eventId = eventId;
        this.organizationId = organizationId;
        this.environment = environment;
        this.metricType = metricType;
        this.module = module;
        this.amount = amount;
        this.currency = currency;
        this.metadata = metadata;
        this.occurredAt = occurredAt;
    }

    public static AnalyticsMetricEvent create(
            Long id,
            String eventId,
            Long organizationId,
            String environment,
            String metricType,
            String module,
            BigDecimal amount,
            String currency,
            String metadata,
            ZonedDateTime occurredAt
    ) {
        if (organizationId == null) {
            throw new AnalyticsInvariantException("Organization ID must not be null");
        }
        if (metricType == null || metricType.isBlank()) {
            throw new AnalyticsInvariantException("Metric type must not be blank");
        }
        if (module == null || module.isBlank()) {
            throw new AnalyticsInvariantException("Module must not be blank");
        }
        return new AnalyticsMetricEvent(
                id,
                eventId,
                organizationId,
                environment != null ? environment : "live",
                metricType,
                module,
                amount,
                currency != null ? currency : "NGN",
                metadata != null ? metadata : "{}",
                occurredAt != null ? occurredAt : ZonedDateTime.now()
        );
    }

    @Override
    public Long getId() {
        return id;
    }
}
