package com.atlashub.analytics.infrastructure.persistence.entities;

import com.atlashub.shared.infrastructure.persistence.entities.BaseJpaEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

@Entity
@Table(
        name = "analytics_metric_events",
        indexes = {
                @Index(name = "idx_metric_events_org_time", columnList = "organization_id, environment, occurred_at"),
                @Index(name = "idx_metric_events_type_time", columnList = "metric_type, occurred_at")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class AnalyticsMetricEventJpa implements BaseJpaEntity {

    @Id
    private Long id;

    @Column(name = "event_id", nullable = false)
    private String eventId;

    @Column(name = "organization_id", nullable = false)
    private Long organizationId;

    @Column(name = "environment", nullable = false)
    private String environment;

    @Column(name = "metric_type", nullable = false)
    private String metricType;

    @Column(name = "module", nullable = false)
    private String module;

    @Column(name = "amount", precision = 19, scale = 4)
    private BigDecimal amount;

    @Column(name = "currency", length = 3)
    private String currency;

    @Column(name = "metadata", columnDefinition = "json")
    private String metadata;

    @Column(name = "occurred_at", nullable = false)
    private ZonedDateTime occurredAt;

    @Version
    private Long version;
}
