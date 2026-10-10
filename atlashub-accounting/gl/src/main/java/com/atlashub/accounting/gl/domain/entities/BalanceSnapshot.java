package com.atlashub.accounting.gl.domain.entities;

import com.atlashub.shared.domain.entities.AggregateRoot;
import com.atlashub.shared.domain.valueobject.Money;
import lombok.Getter;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.Objects;

@Getter
public class BalanceSnapshot extends AggregateRoot<Long> {

    private final Long id;
    private final Long accountId;
    private final LocalDate snapshotDate;
    private final Money balance;
    private final ZonedDateTime createdAt;

    public BalanceSnapshot(Long id,
                           Long accountId,
                           LocalDate snapshotDate,
                           Money balance,
                           ZonedDateTime createdAt) {
        this.id = id;
        this.accountId = accountId;
        this.snapshotDate = snapshotDate;
        this.balance = balance;
        this.createdAt = createdAt;
    }

    public static BalanceSnapshot create(Long id,
                                         Long accountId,
                                         LocalDate snapshotDate,
                                         Money balance) {
        Objects.requireNonNull(id, "BalanceSnapshot ID must not be null");
        Objects.requireNonNull(accountId, "Account ID must not be null");
        Objects.requireNonNull(snapshotDate, "SnapshotDate must not be null");
        Objects.requireNonNull(balance, "Balance must not be null");

        return new BalanceSnapshot(
                id,
                accountId,
                snapshotDate,
                balance,
                ZonedDateTime.now()
        );
    }

    @Override
    public Long getId() {
        return id;
    }
}
