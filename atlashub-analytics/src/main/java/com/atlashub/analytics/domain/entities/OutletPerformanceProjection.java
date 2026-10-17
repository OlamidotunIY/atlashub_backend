package com.atlashub.analytics.domain.entities;

import com.atlashub.analytics.domain.exceptions.AnalyticsInvariantException;
import com.atlashub.shared.domain.entities.AggregateRoot;
import lombok.Getter;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.ZonedDateTime;

@Getter
public class OutletPerformanceProjection extends AggregateRoot<Long> {

    private final Long id;
    private final Long organizationId;
    private final String environment;
    private final Long outletId;
    private final String metricMonth;
    private BigDecimal totalSales;
    private BigDecimal totalDiscounts;
    private BigDecimal totalExpenses;
    private BigDecimal grossMargin;
    private BigDecimal netProfit;
    private int transactionCount;
    private ZonedDateTime updatedAt;

    public OutletPerformanceProjection(
            Long id,
            Long organizationId,
            String environment,
            Long outletId,
            String metricMonth,
            BigDecimal totalSales,
            BigDecimal totalDiscounts,
            BigDecimal totalExpenses,
            BigDecimal grossMargin,
            BigDecimal netProfit,
            int transactionCount,
            ZonedDateTime updatedAt
    ) {
        this.id = id;
        this.organizationId = organizationId;
        this.environment = environment;
        this.outletId = outletId;
        this.metricMonth = metricMonth;
        this.totalSales = totalSales;
        this.totalDiscounts = totalDiscounts;
        this.totalExpenses = totalExpenses;
        this.grossMargin = grossMargin;
        this.netProfit = netProfit;
        this.transactionCount = transactionCount;
        this.updatedAt = updatedAt;
    }

    public static OutletPerformanceProjection create(
            Long id,
            Long organizationId,
            String environment,
            Long outletId,
            String metricMonth
    ) {
        if (organizationId == null) {
            throw new AnalyticsInvariantException("Organization ID must not be null");
        }
        if (outletId == null) {
            throw new AnalyticsInvariantException("Outlet ID must not be null");
        }
        if (metricMonth == null) {
            throw new AnalyticsInvariantException("Metric month must not be null");
        }
        return new OutletPerformanceProjection(
                id,
                organizationId,
                environment != null ? environment : "live",
                outletId,
                metricMonth,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                0,
                ZonedDateTime.now()
        );
    }

    public void applySales(BigDecimal sales, BigDecimal discounts, int count) {
        this.totalSales = this.totalSales.add(sales != null ? sales : BigDecimal.ZERO);
        this.totalDiscounts = this.totalDiscounts.add(discounts != null ? discounts : BigDecimal.ZERO);
        this.transactionCount += count;
        recalculateMargins();
    }

    public void applyExpense(BigDecimal expense) {
        this.totalExpenses = this.totalExpenses.add(expense != null ? expense : BigDecimal.ZERO);
        recalculateMargins();
    }

    private void recalculateMargins() {
        this.netProfit = this.totalSales.subtract(this.totalDiscounts).subtract(this.totalExpenses);
        if (this.totalSales.compareTo(BigDecimal.ZERO) > 0) {
            this.grossMargin = this.netProfit.divide(this.totalSales, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100));
        } else {
            this.grossMargin = BigDecimal.ZERO;
        }
        this.updatedAt = ZonedDateTime.now();
    }

    @Override
    public Long getId() {
        return id;
    }
}
