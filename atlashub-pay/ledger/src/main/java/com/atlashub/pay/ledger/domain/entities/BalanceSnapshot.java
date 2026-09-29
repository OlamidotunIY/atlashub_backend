package com.atlashub.pay.ledger.domain.entities;

import com.atlashub.pay.ledger.domain.exceptions.LedgerInvariantException;
import com.atlashub.shared.domain.valueobject.Money;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.ZonedDateTime;

@Getter
@AllArgsConstructor(access = AccessLevel.PUBLIC)
public class BalanceSnapshot {

    private final Long id;
    private final Long accountId;
    private final Money balance;
    private final ZonedDateTime snapshotAt;

    public static BalanceSnapshot create(Long id, Long accountId, Money balance, ZonedDateTime snapshotAt) {
        if (id == null) {
            throw new LedgerInvariantException("id cannot be null");
        }
        if (accountId == null) {
            throw new LedgerInvariantException("accountId cannot be null");
        }
        if (balance == null) {
            throw new LedgerInvariantException("balance cannot be null");
        }
        if (snapshotAt == null) {
            throw new LedgerInvariantException("snapshotAt cannot be null");
        }
        
        return new BalanceSnapshot(id, accountId, balance, snapshotAt);
    }
}
