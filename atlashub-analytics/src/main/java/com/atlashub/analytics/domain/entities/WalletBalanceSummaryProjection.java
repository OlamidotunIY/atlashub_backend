package com.atlashub.analytics.domain.entities;

import com.atlashub.analytics.domain.exceptions.AnalyticsInvariantException;
import com.atlashub.shared.domain.entities.AggregateRoot;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

@Getter
public class WalletBalanceSummaryProjection extends AggregateRoot<Long> {

    private final Long id;
    private final Long organizationId;
    private final String environment;
    private BigDecimal operatingBalance;
    private BigDecimal escrowBalance;
    private BigDecimal payrollReserveBalance;
    private BigDecimal taxHoldingBalance;
    private ZonedDateTime updatedAt;

    public WalletBalanceSummaryProjection(
            Long id,
            Long organizationId,
            String environment,
            BigDecimal operatingBalance,
            BigDecimal escrowBalance,
            BigDecimal payrollReserveBalance,
            BigDecimal taxHoldingBalance,
            ZonedDateTime updatedAt
    ) {
        this.id = id;
        this.organizationId = organizationId;
        this.environment = environment;
        this.operatingBalance = operatingBalance;
        this.escrowBalance = escrowBalance;
        this.payrollReserveBalance = payrollReserveBalance;
        this.taxHoldingBalance = taxHoldingBalance;
        this.updatedAt = updatedAt;
    }

    public static WalletBalanceSummaryProjection create(
            Long id,
            Long organizationId,
            String environment
    ) {
        if (organizationId == null) {
            throw new AnalyticsInvariantException("Organization ID must not be null");
        }
        return new WalletBalanceSummaryProjection(
                id,
                organizationId,
                environment != null ? environment : "live",
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                ZonedDateTime.now()
        );
    }

    public void updateBalance(String ledgerType, BigDecimal balance) {
        BigDecimal b = balance != null ? balance : BigDecimal.ZERO;
        if ("OPERATING".equalsIgnoreCase(ledgerType)) {
            this.operatingBalance = b;
        } else if ("ESCROW".equalsIgnoreCase(ledgerType)) {
            this.escrowBalance = b;
        } else if ("PAYROLL_RESERVE".equalsIgnoreCase(ledgerType)) {
            this.payrollReserveBalance = b;
        } else if ("TAX_HOLDING".equalsIgnoreCase(ledgerType)) {
            this.taxHoldingBalance = b;
        }
        this.updatedAt = ZonedDateTime.now();
    }

    @Override
    public Long getId() {
        return id;
    }
}
