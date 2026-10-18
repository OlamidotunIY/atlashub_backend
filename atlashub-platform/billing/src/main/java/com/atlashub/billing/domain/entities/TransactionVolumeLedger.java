package com.atlashub.billing.domain.entities;

import com.atlashub.billing.domain.exceptions.BillingDomainException;
import com.atlashub.shared.domain.entities.AggregateRoot;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

@Getter
public class TransactionVolumeLedger extends AggregateRoot<Long> {

    private final Long id;
    private final Long organizationId;
    private final String month;
    private BigDecimal totalVolume;
    private Long totalTransactionCount;
    private final String currency;
    private ZonedDateTime lastUpdatedAt;

    public TransactionVolumeLedger(
            Long id,
            Long organizationId,
            String month,
            BigDecimal totalVolume,
            Long totalTransactionCount,
            String currency,
            ZonedDateTime lastUpdatedAt
    ) {
        this.id = id;
        this.organizationId = organizationId;
        this.month = month;
        this.totalVolume = totalVolume;
        this.totalTransactionCount = totalTransactionCount;
        this.currency = currency;
        this.lastUpdatedAt = lastUpdatedAt;
    }

    public static TransactionVolumeLedger create(
            Long id,
            Long organizationId,
            String month,
            String currency
    ) {
        if (id == null) {
            throw new BillingDomainException("Ledger ID cannot be null");
        }
        if (organizationId == null) {
            throw new BillingDomainException("Organization ID cannot be null");
        }
        if (month == null || month.isBlank()) {
            throw new BillingDomainException("Month cannot be blank");
        }

        return new TransactionVolumeLedger(
                id,
                organizationId,
                month,
                BigDecimal.ZERO,
                0L,
                currency != null ? currency : "NGN",
                ZonedDateTime.now()
        );
    }

    public void recordTransaction(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new BillingDomainException("Transaction amount cannot be negative");
        }

        this.totalVolume = this.totalVolume.add(amount);
        this.totalTransactionCount = this.totalTransactionCount + 1L;
        this.lastUpdatedAt = ZonedDateTime.now();
    }

    @Override
    public Long getId() {
        return id;
    }
}
