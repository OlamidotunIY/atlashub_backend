package com.atlashub.ledger.domain.model;

import com.atlashub.ledger.domain.valueobject.EntryType;
import com.atlashub.shared.domain.AggregateRoot;
import com.atlashub.shared.domain.money.Money;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

@Getter
public class LedgerEntry extends AggregateRoot<Long> {
    private final Long id;
    private final Long accountId;
    private final Money amount;
    private final EntryType type;
    private final TransactionReference transactionReference;
    private final String description;
    private final Money runningBalance;
    private final ZonedDateTime createdAt;

    public LedgerEntry(Long id, Long accountId, Money amount, EntryType type, TransactionReference transactionReference, String description, Money runningBalance, ZonedDateTime createdAt) {
        if (accountId == null) {
            throw new IllegalArgumentException("Account ID cannot be null");
        }
        if (amount == null || amount.amount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }
        if (type == null) {
            throw new IllegalArgumentException("Entry type cannot be null");
        }
        if (description == null || description.trim().isEmpty()) {
            throw new IllegalArgumentException("Description cannot be empty");
        }

        this.id = id;
        this.accountId = accountId;
        this.amount = amount;
        this.type = type;
        this.transactionReference = transactionReference;
        this.description = description;
        this.runningBalance = runningBalance;
        this.createdAt = createdAt != null ? createdAt : ZonedDateTime.now();
    }

    public static LedgerEntry create(Long id, Long accountId, Money amount, EntryType type, TransactionReference transactionReference, String description) {
        return new LedgerEntry(id, accountId, amount, type, transactionReference, description, null, ZonedDateTime.now());
    }

    public LedgerEntry withRunningBalance(Money newRunningBalance) {
        return new LedgerEntry(
                this.id,
                this.accountId,
                this.amount,
                this.type,
                this.transactionReference,
                this.description,
                newRunningBalance,
                this.createdAt
        );
    }

}
