package com.atlashub.pay.txquery.domain.entities;

import com.atlashub.pay.txquery.domain.exceptions.InvalidTransactionRecordException;
import com.atlashub.pay.txquery.domain.valueobject.TransactionDirection;
import com.atlashub.shared.domain.valueobject.Money;
import lombok.Getter;

@Getter
public class TransactionAccountEntry {
    private final Long id;
    private final Long transactionRecordId;
    private final Long ledgerAccountId;
    private final String accountType;
    private final String accountName;
    private final String entryType;
    private final TransactionDirection direction;
    private final Money amount;

    public TransactionAccountEntry(Long id, Long transactionRecordId, Long ledgerAccountId, String accountType,
                                   String accountName, String entryType, TransactionDirection direction, Money amount) {
        if (id == null || transactionRecordId == null || ledgerAccountId == null || amount == null
                || entryType == null || direction == null) {
            throw new InvalidTransactionRecordException("Complete transaction account entry data is required");
        }
        this.id = id;
        this.transactionRecordId = transactionRecordId;
        this.ledgerAccountId = ledgerAccountId;
        this.accountType = accountType;
        this.accountName = accountName;
        this.entryType = entryType;
        this.direction = direction;
        this.amount = amount;
    }
}
