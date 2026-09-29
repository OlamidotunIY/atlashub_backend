package com.atlashub.pay.ledger.domain.entities;

import com.atlashub.pay.ledger.domain.events.LedgerAccountClosedEvent;
import com.atlashub.pay.ledger.domain.events.LedgerAccountFrozenEvent;
import com.atlashub.pay.ledger.domain.events.LedgerAccountUnfrozenEvent;
import com.atlashub.pay.ledger.domain.exceptions.LedgerAccountClosedException;
import com.atlashub.pay.ledger.domain.exceptions.LedgerAccountNotEmptyException;
import com.atlashub.pay.ledger.domain.valueobject.LedgerAccountStatus;
import com.atlashub.pay.ledger.domain.valueobject.LedgerAccountType;
import com.atlashub.shared.domain.entities.AggregateRoot;
import com.atlashub.shared.domain.valueobject.CorrelationId;
import com.atlashub.shared.domain.valueobject.CurrencyCode;
import com.atlashub.shared.domain.valueobject.Money;
import lombok.Getter;

import java.time.ZonedDateTime;
import java.util.UUID;

@Getter
public class LedgerAccount extends AggregateRoot<Long> {

    private final Long id;
    private final Long organizationId;
    private final LedgerAccountType accountType;
    private final Long outletId;
    private final CurrencyCode currency;
    private LedgerAccountStatus status;
    private final ZonedDateTime createdAt;
    private ZonedDateTime updatedAt;

    public LedgerAccount(Long id, Long organizationId, LedgerAccountType accountType,
                         Long outletId, CurrencyCode currency, LedgerAccountStatus status,
                         ZonedDateTime createdAt, ZonedDateTime updatedAt) {
        this.id = id;
        this.organizationId = organizationId;
        this.accountType = accountType;
        this.outletId = outletId;
        this.currency = currency;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static LedgerAccount create(Long id, Long organizationId, LedgerAccountType accountType,
                                       Long outletId, CurrencyCode currency) {
        if (id == null) {
            throw new IllegalArgumentException("id cannot be null");
        }
        if (organizationId == null) {
            throw new IllegalArgumentException("organizationId cannot be null");
        }
        if (accountType == null) {
            throw new IllegalArgumentException("accountType cannot be null");
        }
        if (currency == null) {
            throw new IllegalArgumentException("currency cannot be null");
        }
        ZonedDateTime now = ZonedDateTime.now();
        return new LedgerAccount(
                id,
                organizationId,
                accountType,
                outletId,
                currency,
                LedgerAccountStatus.ACTIVE,
                now,
                now
        );
    }

    public void freeze() {
        if (this.status == LedgerAccountStatus.CLOSED) {
            throw new LedgerAccountClosedException(this.id.toString());
        }
        if (this.status != LedgerAccountStatus.ACTIVE) {
            return;
        }
        this.status = LedgerAccountStatus.FROZEN;
        touch();
        this.registerEvent(new LedgerAccountFrozenEvent(
                UUID.randomUUID().toString(),
                this.id,
                ZonedDateTime.now(),
                CorrelationId.getOrCreate(),
                new LedgerAccountFrozenEvent.Payload(this.id)
        ));
    }

    public void unfreeze() {
        if (this.status == LedgerAccountStatus.CLOSED) {
            throw new LedgerAccountClosedException(this.id.toString());
        }
        if (this.status != LedgerAccountStatus.FROZEN) {
            return;
        }
        this.status = LedgerAccountStatus.ACTIVE;
        touch();
        this.registerEvent(new LedgerAccountUnfrozenEvent(
                UUID.randomUUID().toString(),
                this.id,
                ZonedDateTime.now(),
                CorrelationId.getOrCreate(),
                new LedgerAccountUnfrozenEvent.Payload(this.id)
        ));
    }

    public void close(Money balance) {
        if (this.status == LedgerAccountStatus.CLOSED) {
            return;
        }
        if (balance != null && balance.amount().compareTo(java.math.BigDecimal.ZERO) != 0) {
            throw new LedgerAccountNotEmptyException(this.id.toString());
        }
        this.status = LedgerAccountStatus.CLOSED;
        touch();
        this.registerEvent(new LedgerAccountClosedEvent(
                UUID.randomUUID().toString(),
                this.id,
                ZonedDateTime.now(),
                CorrelationId.getOrCreate(),
                new LedgerAccountClosedEvent.Payload(this.id)
        ));
    }

    private void touch() {
        this.updatedAt = ZonedDateTime.now();
    }

    @Override
    public Long getId() {
        return this.id;
    }
}
