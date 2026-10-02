package com.atlashub.pay.ledger.domain.entities;

import com.atlashub.pay.ledger.domain.events.LedgerAccountClosedEvent;
import com.atlashub.pay.ledger.domain.events.LedgerAccountFrozenEvent;
import com.atlashub.pay.ledger.domain.events.LedgerAccountUnfrozenEvent;
import com.atlashub.pay.ledger.domain.exceptions.LedgerAccountClosedException;
import com.atlashub.pay.ledger.domain.exceptions.LedgerAccountNotEmptyException;
import com.atlashub.pay.ledger.domain.valueobject.LedgerAccountStatus;
import com.atlashub.pay.ledger.domain.valueobject.LedgerAccountType;
import com.atlashub.pay.ledger.domain.valueobject.NormalBalance;
import com.atlashub.pay.ledger.domain.valueobject.LedgerRestrictionType;
import com.atlashub.shared.domain.entities.AggregateRoot;
import com.atlashub.shared.domain.valueobject.CorrelationId;
import com.atlashub.shared.domain.valueobject.CurrencyCode;
import com.atlashub.shared.domain.valueobject.Money;
import lombok.Getter;

import java.time.ZonedDateTime;
import java.util.UUID;
import java.util.HashSet;
import java.util.Set;

@Getter
public class LedgerAccount extends AggregateRoot<Long> {

    private final Long id;
    private final Long organizationId;
    private final LedgerAccountType accountType;
    private final Long outletId;
    private final String partyType;
    private final String partyReferenceId;
    private final CurrencyCode currency;
    private final NormalBalance normalBalance;
    private LedgerAccountStatus status;
    private final Set<LedgerRestrictionType> activeRestrictions;
    private final ZonedDateTime createdAt;
    private ZonedDateTime updatedAt;

    public LedgerAccount(Long id, Long organizationId, LedgerAccountType accountType,
                         Long outletId, String partyType, String partyReferenceId,
                         CurrencyCode currency, NormalBalance normalBalance, LedgerAccountStatus status,
                         Set<LedgerRestrictionType> activeRestrictions,
                         ZonedDateTime createdAt, ZonedDateTime updatedAt) {
        this.id = id;
        this.organizationId = organizationId;
        this.accountType = accountType;
        this.outletId = outletId;
        this.partyType = partyType;
        this.partyReferenceId = partyReferenceId;
        this.currency = currency;
        this.normalBalance = normalBalance;
        this.status = status;
        this.activeRestrictions = activeRestrictions == null ? new HashSet<>() : new HashSet<>(activeRestrictions);
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static LedgerAccount create(Long id, Long organizationId, LedgerAccountType accountType,
                                       Long outletId, String partyType, String partyReferenceId,
                                       CurrencyCode currency, NormalBalance normalBalance) {
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
        if (normalBalance == null) {
            throw new IllegalArgumentException("normalBalance cannot be null");
        }
        boolean partyAccount = accountType == LedgerAccountType.CUSTOMER_FUNDS
                || accountType == LedgerAccountType.VENDOR_PAYABLE;
        if (partyAccount && (partyType == null || partyReferenceId == null || partyReferenceId.isBlank())) {
            throw new IllegalArgumentException("Party type and reference are required for party ledger accounts");
        }
        ZonedDateTime now = ZonedDateTime.now();
        return new LedgerAccount(
                id,
                organizationId,
                accountType,
                outletId,
                partyType,
                partyReferenceId,
                currency,
                normalBalance,
                LedgerAccountStatus.ACTIVE,
                Set.of(),
                now,
                now
        );
    }

    public void freeze(LedgerRestrictionType restrictionType) {
        if (this.status == LedgerAccountStatus.CLOSED) {
            throw new LedgerAccountClosedException(this.id.toString());
        }
        if (!activeRestrictions.add(restrictionType)) {
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

    public void unfreeze(LedgerRestrictionType restrictionType) {
        if (this.status == LedgerAccountStatus.CLOSED) {
            throw new LedgerAccountClosedException(this.id.toString());
        }
        if (!activeRestrictions.remove(restrictionType)) {
            return;
        }
        if (!activeRestrictions.isEmpty()) {
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
