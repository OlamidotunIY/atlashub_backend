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
import java.time.LocalDate;
import java.time.ZonedDateTime;

@Entity
@Table(
        name = "analytics_daily_sales",
        indexes = {
                @Index(name = "idx_daily_sales_org_date", columnList = "organization_id, metric_date"),
                @Index(name = "idx_daily_sales_unique", columnList = "organization_id, environment, outlet_id, metric_date", unique = true)
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class DailySalesProjectionJpa implements BaseJpaEntity {

    @Id
    private Long id;

    @Column(name = "organization_id", nullable = false)
    private Long organizationId;

    @Column(name = "environment", nullable = false)
    private String environment;

    @Column(name = "outlet_id", nullable = false)
    private Long outletId;

    @Column(name = "metric_date", nullable = false)
    private LocalDate metricDate;

    @Column(name = "total_transactions", nullable = false)
    private int totalTransactions;

    @Column(name = "total_gross", precision = 19, scale = 4)
    private BigDecimal totalGross;

    @Column(name = "total_discounts", precision = 19, scale = 4)
    private BigDecimal totalDiscounts;

    @Column(name = "total_tax", precision = 19, scale = 4)
    private BigDecimal totalTax;

    @Column(name = "total_net", precision = 19, scale = 4)
    private BigDecimal totalNet;

    @Column(name = "cash_sales", precision = 19, scale = 4)
    private BigDecimal cashSales;

    @Column(name = "card_sales", precision = 19, scale = 4)
    private BigDecimal cardSales;

    @Column(name = "credit_sales", precision = 19, scale = 4)
    private BigDecimal creditSales;

    @Column(name = "updated_at")
    private ZonedDateTime updatedAt;

    @Version
    private Long version;
}
