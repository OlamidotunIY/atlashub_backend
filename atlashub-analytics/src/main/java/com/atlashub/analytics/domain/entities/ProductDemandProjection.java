package com.atlashub.analytics.domain.entities;

import com.atlashub.analytics.domain.exceptions.AnalyticsInvariantException;
import com.atlashub.shared.domain.entities.AggregateRoot;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

@Getter
public class ProductDemandProjection extends AggregateRoot<Long> {

    private final Long id;
    private final Long organizationId;
    private final String environment;
    private final Long productId;
    private final Long outletId;
    private final String metricMonth;
    private int quantitySold;
    private BigDecimal totalRevenue;
    private ZonedDateTime updatedAt;

    public ProductDemandProjection(
            Long id,
            Long organizationId,
            String environment,
            Long productId,
            Long outletId,
            String metricMonth,
            int quantitySold,
            BigDecimal totalRevenue,
            ZonedDateTime updatedAt
    ) {
        this.id = id;
        this.organizationId = organizationId;
        this.environment = environment;
        this.productId = productId;
        this.outletId = outletId;
        this.metricMonth = metricMonth;
        this.quantitySold = quantitySold;
        this.totalRevenue = totalRevenue;
        this.updatedAt = updatedAt;
    }

    public static ProductDemandProjection create(
            Long id,
            Long organizationId,
            String environment,
            Long productId,
            Long outletId,
            String metricMonth
    ) {
        if (organizationId == null) {
            throw new AnalyticsInvariantException("Organization ID must not be null");
        }
        if (productId == null) {
            throw new AnalyticsInvariantException("Product ID must not be null");
        }
        if (metricMonth == null) {
            throw new AnalyticsInvariantException("Metric month must not be null");
        }
        return new ProductDemandProjection(
                id,
                organizationId,
                environment != null ? environment : "live",
                productId,
                outletId != null ? outletId : 0L,
                metricMonth,
                0,
                BigDecimal.ZERO,
                ZonedDateTime.now()
        );
    }

    public void applySale(int quantity, BigDecimal revenue) {
        this.quantitySold += quantity;
        this.totalRevenue = this.totalRevenue.add(revenue != null ? revenue : BigDecimal.ZERO);
        this.updatedAt = ZonedDateTime.now();
    }

    public void applyRefund(int quantity, BigDecimal revenue) {
        this.quantitySold = Math.max(0, this.quantitySold - quantity);
        this.totalRevenue = this.totalRevenue.subtract(revenue != null ? revenue : BigDecimal.ZERO).max(BigDecimal.ZERO);
        this.updatedAt = ZonedDateTime.now();
    }

    @Override
    public Long getId() {
        return id;
    }
}
