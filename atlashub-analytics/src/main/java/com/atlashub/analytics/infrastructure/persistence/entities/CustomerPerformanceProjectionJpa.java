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
        name = "analytics_customer_performance",
        indexes = {
                @Index(name = "idx_customer_perf_org_month", columnList = "organization_id, metric_month"),
                @Index(name = "idx_customer_perf_unique", columnList = "organization_id, environment, customer_id, metric_month", unique = true)
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class CustomerPerformanceProjectionJpa implements BaseJpaEntity {

    @Id
    private Long id;

    @Column(name = "organization_id", nullable = false)
    private Long organizationId;

    @Column(name = "environment", nullable = false)
    private String environment;

    @Column(name = "customer_id", nullable = false)
    private Long customerId;

    @Column(name = "metric_month", nullable = false, length = 7)
    private String metricMonth;

    @Column(name = "total_orders", nullable = false)
    private int totalOrders;

    @Column(name = "total_spent", precision = 19, scale = 4)
    private BigDecimal totalSpent;

    @Column(name = "refunded_orders", nullable = false)
    private int refundedOrders;

    @Column(name = "refunded_amount", precision = 19, scale = 4)
    private BigDecimal refundedAmount;

    @Column(name = "updated_at")
    private ZonedDateTime updatedAt;

    @Version
    private Long version;
}
