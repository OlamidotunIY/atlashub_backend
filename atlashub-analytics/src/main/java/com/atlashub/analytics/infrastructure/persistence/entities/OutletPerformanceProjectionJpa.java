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
        name = "analytics_outlet_performance",
        indexes = {
                @Index(name = "idx_outlet_perf_org_month", columnList = "organization_id, metric_month"),
                @Index(name = "idx_outlet_perf_unique", columnList = "organization_id, environment, outlet_id, metric_month", unique = true)
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class OutletPerformanceProjectionJpa implements BaseJpaEntity {

    @Id
    private Long id;

    @Column(name = "organization_id", nullable = false)
    private Long organizationId;

    @Column(name = "environment", nullable = false)
    private String environment;

    @Column(name = "outlet_id", nullable = false)
    private Long outletId;

    @Column(name = "metric_month", nullable = false, length = 7)
    private String metricMonth;

    @Column(name = "total_sales", precision = 19, scale = 4)
    private BigDecimal totalSales;

    @Column(name = "total_discounts", precision = 19, scale = 4)
    private BigDecimal totalDiscounts;

    @Column(name = "total_expenses", precision = 19, scale = 4)
    private BigDecimal totalExpenses;

    @Column(name = "gross_margin", precision = 7, scale = 4)
    private BigDecimal grossMargin;

    @Column(name = "net_profit", precision = 19, scale = 4)
    private BigDecimal netProfit;

    @Column(name = "transaction_count", nullable = false)
    private int transactionCount;

    @Column(name = "updated_at")
    private ZonedDateTime updatedAt;

    @Version
    private Long version;
}
