package com.atlashub.analytics.domain.entities;

import com.atlashub.analytics.domain.exceptions.AnalyticsInvariantException;
import com.atlashub.shared.domain.entities.AggregateRoot;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

@Getter
public class CustomerPerformanceProjection extends AggregateRoot<Long> {

    private final Long id;
    private final Long organizationId;
    private final String environment;
    private final Long customerId;
    private final String metricMonth;
    private int totalOrders;
    private BigDecimal totalSpent;
    private int refundedOrders;
    private BigDecimal refundedAmount;
    private ZonedDateTime updatedAt;

    public CustomerPerformanceProjection(
            Long id,
            Long organizationId,
            String environment,
            Long customerId,
            String metricMonth,
            int totalOrders,
            BigDecimal totalSpent,
            int refundedOrders,
            BigDecimal refundedAmount,
            ZonedDateTime updatedAt
    ) {
        this.id = id;
        this.organizationId = organizationId;
        this.environment = environment;
        this.customerId = customerId;
        this.metricMonth = metricMonth;
        this.totalOrders = totalOrders;
        this.totalSpent = totalSpent;
        this.refundedOrders = refundedOrders;
        this.refundedAmount = refundedAmount;
        this.updatedAt = updatedAt;
    }

    public static CustomerPerformanceProjection create(
            Long id,
            Long organizationId,
            String environment,
            Long customerId,
            String metricMonth
    ) {
        if (organizationId == null) {
            throw new AnalyticsInvariantException("Organization ID must not be null");
        }
        if (customerId == null) {
            throw new AnalyticsInvariantException("Customer ID must not be null");
        }
        if (metricMonth == null) {
            throw new AnalyticsInvariantException("Metric month must not be null");
        }
        return new CustomerPerformanceProjection(
                id,
                organizationId,
                environment != null ? environment : "live",
                customerId,
                metricMonth,
                0,
                BigDecimal.ZERO,
                0,
                BigDecimal.ZERO,
                ZonedDateTime.now()
        );
    }

    public void applyOrder(BigDecimal amount) {
        this.totalOrders += 1;
        this.totalSpent = this.totalSpent.add(amount != null ? amount : BigDecimal.ZERO);
        this.updatedAt = ZonedDateTime.now();
    }

    public void applyRefund(BigDecimal amount) {
        this.refundedOrders += 1;
        this.refundedAmount = this.refundedAmount.add(amount != null ? amount : BigDecimal.ZERO);
        this.updatedAt = ZonedDateTime.now();
    }

    @Override
    public Long getId() {
        return id;
    }
}
