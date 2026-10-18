package com.atlashub.analytics.domain.entities;

import com.atlashub.analytics.domain.exceptions.AnalyticsInvariantException;
import com.atlashub.shared.domain.entities.AggregateRoot;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

@Getter
public class PayrollCostProjection extends AggregateRoot<Long> {

    private final Long id;
    private final Long organizationId;
    private final String environment;
    private final String metricMonth;
    private BigDecimal totalGrossPayroll;
    private BigDecimal totalDeductions;
    private BigDecimal totalNetPayroll;
    private int headcount;
    private ZonedDateTime updatedAt;

    public PayrollCostProjection(
            Long id,
            Long organizationId,
            String environment,
            String metricMonth,
            BigDecimal totalGrossPayroll,
            BigDecimal totalDeductions,
            BigDecimal totalNetPayroll,
            int headcount,
            ZonedDateTime updatedAt
    ) {
        this.id = id;
        this.organizationId = organizationId;
        this.environment = environment;
        this.metricMonth = metricMonth;
        this.totalGrossPayroll = totalGrossPayroll;
        this.totalDeductions = totalDeductions;
        this.totalNetPayroll = totalNetPayroll;
        this.headcount = headcount;
        this.updatedAt = updatedAt;
    }

    public static PayrollCostProjection create(
            Long id,
            Long organizationId,
            String environment,
            String metricMonth
    ) {
        if (organizationId == null) {
            throw new AnalyticsInvariantException("Organization ID must not be null");
        }
        if (metricMonth == null || metricMonth.isBlank()) {
            throw new AnalyticsInvariantException("Metric month must not be blank");
        }
        return new PayrollCostProjection(
                id,
                organizationId,
                environment != null ? environment : "live",
                metricMonth,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                0,
                ZonedDateTime.now()
        );
    }

    public void applyPayroll(BigDecimal gross, BigDecimal deductions, BigDecimal net, int count) {
        this.totalGrossPayroll = this.totalGrossPayroll.add(gross != null ? gross : BigDecimal.ZERO);
        this.totalDeductions = this.totalDeductions.add(deductions != null ? deductions : BigDecimal.ZERO);
        this.totalNetPayroll = this.totalNetPayroll.add(net != null ? net : BigDecimal.ZERO);
        this.headcount = count;
        this.updatedAt = ZonedDateTime.now();
    }

    @Override
    public Long getId() {
        return id;
    }
}
