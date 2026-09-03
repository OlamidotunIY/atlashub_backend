package com.atlashub.ledger.domain.model;

import com.atlashub.shared.domain.AggregateRoot;
import com.atlashub.shared.domain.money.Money;
import lombok.Getter;

import java.time.ZonedDateTime;

@Getter
public class BalanceSnapshot extends AggregateRoot<Long> {
    private final Long id;
    private final Long accountId;
    private final Money balance;
    private final Long lastLedgerEntryId;
    private final ZonedDateTime snapshotAt;

    public BalanceSnapshot(Long id, Long accountId, Money balance, Long lastLedgerEntryId, ZonedDateTime snapshotAt) {
        if (accountId == null) {
            throw new IllegalArgumentException("Account ID cannot be null");
        }
        if (balance == null) {
            throw new IllegalArgumentException("Balance cannot be null");
        }
        
        this.id = id;
        this.accountId = accountId;
        this.balance = balance;
        this.lastLedgerEntryId = lastLedgerEntryId;
        this.snapshotAt = snapshotAt != null ? snapshotAt : ZonedDateTime.now();
    }

    public static BalanceSnapshot initialize(Long id, Long accountId, Money initialBalance) {
        return new BalanceSnapshot(id, accountId, initialBalance, null, ZonedDateTime.now());
    }

    public BalanceSnapshot update(Money newBalance, Long ledgerEntryId) {
        return new BalanceSnapshot(this.id, this.accountId, newBalance, ledgerEntryId, ZonedDateTime.now());
    }

    @Override
    public Long getId() { return id; }
}
