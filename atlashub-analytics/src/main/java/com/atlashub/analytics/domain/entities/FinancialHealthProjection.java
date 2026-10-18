package com.atlashub.analytics.domain.entities;

import com.atlashub.analytics.domain.exceptions.AnalyticsInvariantException;
import com.atlashub.shared.domain.entities.AggregateRoot;
import lombok.Getter;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.ZonedDateTime;

@Getter
public class FinancialHealthProjection extends AggregateRoot<Long> {

    private final Long id;
    private final Long organizationId;
    private final String environment;
    private final LocalDate periodEnd;
    private BigDecimal revenue;
    private BigDecimal costOfSales;
    private BigDecimal grossProfit;
    private BigDecimal grossMargin;
    private BigDecimal operatingExpense;
    private BigDecimal netOperatingIncome;
    private BigDecimal cashBalance;
    private BigDecimal accountsReceivable;
    private BigDecimal accountsPayable;
    private String reconciliationStatus;
    private ZonedDateTime updatedAt;

    public FinancialHealthProjection(
            Long id,
            Long organizationId,
            String environment,
            LocalDate periodEnd,
            BigDecimal revenue,
            BigDecimal costOfSales,
            BigDecimal grossProfit,
            BigDecimal grossMargin,
            BigDecimal operatingExpense,
            BigDecimal netOperatingIncome,
            BigDecimal cashBalance,
            BigDecimal accountsReceivable,
            BigDecimal accountsPayable,
            String reconciliationStatus,
            ZonedDateTime updatedAt
    ) {
        this.id = id;
        this.organizationId = organizationId;
        this.environment = environment;
        this.periodEnd = periodEnd;
        this.revenue = revenue;
        this.costOfSales = costOfSales;
        this.grossProfit = grossProfit;
        this.grossMargin = grossMargin;
        this.operatingExpense = operatingExpense;
        this.netOperatingIncome = netOperatingIncome;
        this.cashBalance = cashBalance;
        this.accountsReceivable = accountsReceivable;
        this.accountsPayable = accountsPayable;
        this.reconciliationStatus = reconciliationStatus;
        this.updatedAt = updatedAt;
    }

    public static FinancialHealthProjection create(
            Long id,
            Long organizationId,
            String environment,
            LocalDate periodEnd
    ) {
        if (organizationId == null) {
            throw new AnalyticsInvariantException("Organization ID must not be null");
        }
        if (periodEnd == null) {
            throw new AnalyticsInvariantException("Period end date must not be null");
        }
        return new FinancialHealthProjection(
                id,
                organizationId,
                environment != null ? environment : "live",
                periodEnd,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                "RECONCILED",
                ZonedDateTime.now()
        );
    }

    public void updateMetrics(
            BigDecimal rev,
            BigDecimal cos,
            BigDecimal opex,
            BigDecimal cash,
            BigDecimal ar,
            BigDecimal ap,
            String status
    ) {
        this.revenue = rev != null ? rev : BigDecimal.ZERO;
        this.costOfSales = cos != null ? cos : BigDecimal.ZERO;
        this.grossProfit = this.revenue.subtract(this.costOfSales);
        if (this.revenue.compareTo(BigDecimal.ZERO) > 0) {
            this.grossMargin = this.grossProfit.divide(this.revenue, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100));
        } else {
            this.grossMargin = BigDecimal.ZERO;
        }
        this.operatingExpense = opex != null ? opex : BigDecimal.ZERO;
        this.netOperatingIncome = this.grossProfit.subtract(this.operatingExpense);
        this.cashBalance = cash != null ? cash : BigDecimal.ZERO;
        this.accountsReceivable = ar != null ? ar : BigDecimal.ZERO;
        this.accountsPayable = ap != null ? ap : BigDecimal.ZERO;
        if (status != null && !status.isBlank()) {
            this.reconciliationStatus = status;
        }
        this.updatedAt = ZonedDateTime.now();
    }

    @Override
    public Long getId() {
        return id;
    }
}
