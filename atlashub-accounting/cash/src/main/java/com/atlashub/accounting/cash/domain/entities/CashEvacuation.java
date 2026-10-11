package com.atlashub.accounting.cash.domain.entities;

import com.atlashub.accounting.cash.domain.events.CashEvacuatedEvent;
import com.atlashub.accounting.cash.domain.exceptions.InvalidReconciliationStateException;
import com.atlashub.accounting.cash.domain.valueobject.EvacuationStatus;
import com.atlashub.shared.domain.entities.AggregateRoot;
import com.atlashub.shared.domain.valueobject.Money;
import lombok.Getter;

import java.time.ZonedDateTime;

@Getter
public class CashEvacuation extends AggregateRoot<Long> {

    private final Long id;
    private final Long organizationId;
    private final Long outletId;
    private final Long tillId;
    private final Money amount;
    private final Long evacuatedBy;
    private final ZonedDateTime evacuatedAt;
    private EvacuationStatus status;
    private ZonedDateTime confirmedAt;

    public CashEvacuation(Long id,
                          Long organizationId,
                          Long outletId,
                          Long tillId,
                          Money amount,
                          Long evacuatedBy,
                          ZonedDateTime evacuatedAt,
                          EvacuationStatus status,
                          ZonedDateTime confirmedAt) {
        this.id = id;
        this.organizationId = organizationId;
        this.outletId = outletId;
        this.tillId = tillId;
        this.amount = amount;
        this.evacuatedBy = evacuatedBy;
        this.evacuatedAt = evacuatedAt;
        this.status = status;
        this.confirmedAt = confirmedAt;
    }

    public static CashEvacuation create(Long id,
                                        Long organizationId,
                                        Long outletId,
                                        Long tillId,
                                        Money amount,
                                        Long evacuatedBy) {
        if (id == null) {
            throw new InvalidReconciliationStateException("Cash evacuation ID must not be null");
        }
        if (organizationId == null) {
            throw new InvalidReconciliationStateException("Organization ID must not be null");
        }
        if (outletId == null) {
            throw new InvalidReconciliationStateException("Outlet ID must not be null");
        }
        if (tillId == null) {
            throw new InvalidReconciliationStateException("Till ID must not be null");
        }
        if (amount == null || !amount.isPositive()) {
            throw new InvalidReconciliationStateException("Cash evacuation amount must be positive");
        }
        if (evacuatedBy == null) {
            throw new InvalidReconciliationStateException("Evacuated by user ID must not be null");
        }

        ZonedDateTime now = ZonedDateTime.now();
        return new CashEvacuation(
                id,
                organizationId,
                outletId,
                tillId,
                amount,
                evacuatedBy,
                now,
                EvacuationStatus.PENDING,
                null
        );
    }

    public void confirm() {
        if (this.status != EvacuationStatus.PENDING) {
            throw new InvalidReconciliationStateException("Cash evacuation is already confirmed");
        }

        this.status = EvacuationStatus.CONFIRMED;
        this.confirmedAt = ZonedDateTime.now();

        registerEvent(CashEvacuatedEvent.of(
                this.id,
                this.organizationId,
                this.outletId,
                this.tillId,
                this.amount.amount(),
                this.amount.currency().name(),
                this.evacuatedBy,
                this.confirmedAt
        ));
    }

    @Override
    public Long getId() {
        return id;
    }
}
