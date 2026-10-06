package com.atlashub.pay.ledger.domain.entities;

import com.atlashub.pay.ledger.domain.exceptions.LedgerInvariantException;
import com.atlashub.pay.ledger.domain.valueobject.EntryType;
import com.atlashub.shared.domain.valueobject.Money;
import lombok.Getter;

@Getter
public class LedgerEntry {

    private final Long id;
    private final Long transactionId;
    private final Long accountId;
    private final EntryType type;
    private final Money amount;

    public LedgerEntry(Long id, Long transactionId, Long accountId, EntryType type, Money amount) {
        this.id = id;
        this.transactionId = transactionId;
        this.accountId = accountId;
        this.type = type;
        this.amount = amount;
    }

    public static LedgerEntry create(Long id, Long transactionId, Long accountId, EntryType type, Money amount) {
        if (id == null) {
            throw new LedgerInvariantException("LedgerEntry id cannot be null");
        }
        if (transactionId == null) {
            throw new LedgerInvariantException("LedgerEntry transactionId cannot be null");
        }
        if (accountId == null) {
            throw new LedgerInvariantException("LedgerEntry accountId cannot be null");
        }
        if (type == null) {
            throw new LedgerInvariantException("LedgerEntry type cannot be null");
        }
        if (amount == null) {
            throw new LedgerInvariantException("LedgerEntry amount cannot be null");
        }
        return new LedgerEntry(id, transactionId, accountId, type, amount);
    }
}
