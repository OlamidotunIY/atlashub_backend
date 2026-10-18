package com.atlashub.analytics.domain.entities;

import com.atlashub.analytics.domain.exceptions.AnalyticsInvariantException;
import com.atlashub.shared.domain.entities.AggregateRoot;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

@Getter
public class TransactionVolumeProjection extends AggregateRoot<Long> {

    private final Long id;
    private final Long organizationId;
    private final String environment;
    private final String metricMonth;
    private long totalCharges;
    private BigDecimal totalVolume;
    private long successfulCharges;
    private long failedCharges;
    private long totalPayouts;
    private BigDecimal totalPayoutAmount;
    private BigDecimal platformFeeCollected;
    private ZonedDateTime updatedAt;

    public TransactionVolumeProjection(
            Long id,
            Long organizationId,
            String environment,
            String metricMonth,
            long totalCharges,
            BigDecimal totalVolume,
            long successfulCharges,
            long failedCharges,
            long totalPayouts,
            BigDecimal totalPayoutAmount,
            BigDecimal platformFeeCollected,
            ZonedDateTime updatedAt
    ) {
        this.id = id;
        this.organizationId = organizationId;
        this.environment = environment;
        this.metricMonth = metricMonth;
        this.totalCharges = totalCharges;
        this.totalVolume = totalVolume;
        this.successfulCharges = successfulCharges;
        this.failedCharges = failedCharges;
        this.totalPayouts = totalPayouts;
        this.totalPayoutAmount = totalPayoutAmount;
        this.platformFeeCollected = platformFeeCollected;
        this.updatedAt = updatedAt;
    }

    public static TransactionVolumeProjection create(
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
        return new TransactionVolumeProjection(
                id,
                organizationId,
                environment != null ? environment : "live",
                metricMonth,
                0L,
                BigDecimal.ZERO,
                0L,
                0L,
                0L,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                ZonedDateTime.now()
        );
    }

    public void applyCharge(BigDecimal amount, BigDecimal fee, boolean successful) {
        this.totalCharges += 1;
        BigDecimal amt = amount != null ? amount : BigDecimal.ZERO;
        BigDecimal f = fee != null ? fee : BigDecimal.ZERO;

        if (successful) {
            this.successfulCharges += 1;
            this.totalVolume = this.totalVolume.add(amt);
            this.platformFeeCollected = this.platformFeeCollected.add(f);
        } else {
            this.failedCharges += 1;
        }
        this.updatedAt = ZonedDateTime.now();
    }

    public void applyPayout(BigDecimal amount) {
        this.totalPayouts += 1;
        this.totalPayoutAmount = this.totalPayoutAmount.add(amount != null ? amount : BigDecimal.ZERO);
        this.updatedAt = ZonedDateTime.now();
    }

    @Override
    public Long getId() {
        return id;
    }
}
