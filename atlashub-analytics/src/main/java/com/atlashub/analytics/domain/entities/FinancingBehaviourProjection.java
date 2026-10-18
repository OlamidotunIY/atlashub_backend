package com.atlashub.analytics.domain.entities;

import com.atlashub.analytics.domain.exceptions.AnalyticsInvariantException;
import com.atlashub.shared.domain.entities.AggregateRoot;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

@Getter
public class FinancingBehaviourProjection extends AggregateRoot<Long> {

    private final Long id;
    private final Long organizationId;
    private final String environment;
    private final String paymentContextKey;
    private BigDecimal totalInflow;
    private BigDecimal totalOutflow;
    private BigDecimal averageDailyBalance;
    private BigDecimal repaymentSuccessRate;
    private int anomalyCount;
    private ZonedDateTime occurredAt;

    public FinancingBehaviourProjection(
            Long id,
            Long organizationId,
            String environment,
            String paymentContextKey,
            BigDecimal totalInflow,
            BigDecimal totalOutflow,
            BigDecimal averageDailyBalance,
            BigDecimal repaymentSuccessRate,
            int anomalyCount,
            ZonedDateTime occurredAt
    ) {
        this.id = id;
        this.organizationId = organizationId;
        this.environment = environment;
        this.paymentContextKey = paymentContextKey;
        this.totalInflow = totalInflow;
        this.totalOutflow = totalOutflow;
        this.averageDailyBalance = averageDailyBalance;
        this.repaymentSuccessRate = repaymentSuccessRate;
        this.anomalyCount = anomalyCount;
        this.occurredAt = occurredAt;
    }

    public static FinancingBehaviourProjection create(
            Long id,
            Long organizationId,
            String environment,
            String paymentContextKey
    ) {
        if (organizationId == null) {
            throw new AnalyticsInvariantException("Organization ID must not be null");
        }
        if (paymentContextKey == null || paymentContextKey.isBlank()) {
            throw new AnalyticsInvariantException("Payment context key must not be blank");
        }
        return new FinancingBehaviourProjection(
                id,
                organizationId,
                environment != null ? environment : "live",
                paymentContextKey,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.valueOf(100.00),
                0,
                ZonedDateTime.now()
        );
    }

    public void recordTransaction(BigDecimal amount, boolean isInflow, boolean isAnomaly) {
        BigDecimal amt = amount != null ? amount : BigDecimal.ZERO;
        if (isInflow) {
            this.totalInflow = this.totalInflow.add(amt);
        } else {
            this.totalOutflow = this.totalOutflow.add(amt);
        }
        if (isAnomaly) {
            this.anomalyCount += 1;
        }
        this.occurredAt = ZonedDateTime.now();
    }

    public void updateRepaymentRate(BigDecimal rate, BigDecimal avgBalance) {
        if (rate != null) {
            this.repaymentSuccessRate = rate;
        }
        if (avgBalance != null) {
            this.averageDailyBalance = avgBalance;
        }
        this.occurredAt = ZonedDateTime.now();
    }

    @Override
    public Long getId() {
        return id;
    }
}
