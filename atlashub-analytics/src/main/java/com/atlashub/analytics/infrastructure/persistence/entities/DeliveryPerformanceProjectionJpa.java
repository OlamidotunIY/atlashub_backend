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
        name = "analytics_delivery_performance",
        indexes = {
                @Index(name = "idx_delivery_perf_org_month", columnList = "organization_id, metric_month"),
                @Index(name = "idx_delivery_perf_unique", columnList = "organization_id, environment, metric_month", unique = true)
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class DeliveryPerformanceProjectionJpa implements BaseJpaEntity {

    @Id
    private Long id;

    @Column(name = "organization_id", nullable = false)
    private Long organizationId;

    @Column(name = "environment", nullable = false)
    private String environment;

    @Column(name = "metric_month", nullable = false, length = 7)
    private String metricMonth;

    @Column(name = "total_shipments", nullable = false)
    private int totalShipments;

    @Column(name = "delivered", nullable = false)
    private int delivered;

    @Column(name = "failed", nullable = false)
    private int failed;

    @Column(name = "returned", nullable = false)
    private int returned;

    @Column(name = "delivery_success_rate", precision = 7, scale = 4)
    private BigDecimal deliverySuccessRate;

    @Column(name = "avg_delivery_minutes", nullable = false)
    private int avgDeliveryMinutes;

    @Column(name = "updated_at")
    private ZonedDateTime updatedAt;

    @Version
    private Long version;
}
