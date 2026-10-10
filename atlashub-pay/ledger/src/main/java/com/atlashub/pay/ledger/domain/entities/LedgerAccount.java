package com.atlashub.pay.ledger.domain.entities;

import com.atlashub.pay.ledger.domain.events.LedgerAccountClosedEvent;
import com.atlashub.pay.ledger.domain.events.LedgerAccountFrozenEvent;
import com.atlashub.pay.ledger.domain.events.LedgerAccountUnfrozenEvent;
import com.atlashub.pay.ledger.domain.exceptions.LedgerAccountClosedException;
import com.atlashub.pay.ledger.domain.exceptions.LedgerAccountNotEmptyException;
import com.atlashub.pay.ledger.domain.valueobject.LedgerAccountStatus;
import com.atlashub.pay.ledger.domain.valueobject.LedgerAccountType;
import com.atlashub.pay.ledger.domain.valueobject.LedgerAccountScope;
import com.atlashub.pay.ledger.domain.valueobject.NormalBalance;
import com.atlashub.pay.ledger.domain.valueobject.LedgerRestrictionType;
import com.atlashub.pay.ledger.domain.valueobject.LedgerPartyType;
import com.atlashub.pay.ledger.domain.exceptions.LedgerInvariantException;
import com.atlashub.shared.application.security.ApiEnvironment;
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
    private final ApiEnvironment environment;
    private final LedgerAccountType accountType;
    private final String accountName;
    private final Long outletId;
    private final LedgerPartyType partyType;
    private final String partyReferenceId;
    private final CurrencyCode currency;
    private final NormalBalance normalBalance;
    private LedgerAccountStatus status;
    private final Set<LedgerRestrictionType> activeRestrictions;
    private final ZonedDateTime createdAt;
    private ZonedDateTime updatedAt;

    public LedgerAccount(Long id, Long organizationId, ApiEnvironment environment, LedgerAccountType accountType,
                         String accountName, Long outletId, LedgerPartyType partyType, String partyReferenceId,
                         CurrencyCode currency, NormalBalance normalBalance, LedgerAccountStatus status,
                         Set<LedgerRestrictionType> activeRestrictions, ZonedDateTime createdAt, ZonedDateTime updatedAt) {
        this.id = id;
        this.organizationId = organizationId;
        this.environment = environment;
        this.accountType = accountType;
        this.accountName = accountName == null || accountName.isBlank() ? defaultName(accountType) : accountName.trim();
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

    public static LedgerAccount create(Long id, Long organizationId, ApiEnvironment environment,
                                       LedgerAccountType accountType, Long outletId,
                                       LedgerPartyType partyType, String partyReferenceId,
                                       CurrencyCode currency, NormalBalance normalBalance) {
        if (id == null) {
            throw new LedgerInvariantException("Ledger account id is required");
        }
        if (organizationId == null) {
            throw new LedgerInvariantException("Ledger account organization is required");
        }
        if (environment == null) throw new LedgerInvariantException("Ledger account environment is required");
        if (accountType == null) {
            throw new LedgerInvariantException("Ledger account type is required");
        }
        if (currency == null) {
            throw new LedgerInvariantException("Ledger account currency is required");
        }
        if (normalBalance == null) {
            throw new LedgerInvariantException("Ledger account normal balance is required");
        }
        boolean partyAccount = accountType == LedgerAccountType.CUSTOMER_FUNDS
                || accountType == LedgerAccountType.VENDOR_PAYABLE;
        if (partyAccount && (partyType == null || partyReferenceId == null || partyReferenceId.isBlank())) {
            throw new LedgerInvariantException("Party type and reference are required for party ledger accounts");
        }
        if (!partyAccount && (partyType != null || partyReferenceId != null))
            throw new LedgerInvariantException("Only party ledger accounts may have party ownership");
        if (accountType == LedgerAccountType.TILL && outletId == null)
            throw new LedgerInvariantException("TILL ledger accounts require an outlet");
        if (accountType != LedgerAccountType.TILL && outletId != null)
            throw new LedgerInvariantException("Only TILL ledger accounts may have an outlet");
        ZonedDateTime now = ZonedDateTime.now();
        return new LedgerAccount(
                id,
                organizationId,
                environment,
                accountType,
                defaultName(accountType),
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

    public static LedgerAccount create(Long id, Long organizationId, ApiEnvironment environment,
                                       LedgerAccountType accountType, String accountName, Long outletId,
                                       LedgerPartyType partyType, String partyReferenceId, CurrencyCode currency,
                                       NormalBalance normalBalance) {
        LedgerAccount account = create(id, organizationId, environment, accountType, outletId, partyType,
                partyReferenceId, currency, normalBalance);
        return new LedgerAccount(account.id, account.organizationId, account.environment, account.accountType,
                accountName, account.outletId, account.partyType, account.partyReferenceId, account.currency,
                account.normalBalance, account.status, account.activeRestrictions, account.createdAt, account.updatedAt);
    }

    public LedgerAccountScope scope() {
        if (accountType == LedgerAccountType.PROVIDER_CLEARING || accountType == LedgerAccountType.SUSPENSE)
            return LedgerAccountScope.SYSTEM;
        if (accountType == LedgerAccountType.TILL) return LedgerAccountScope.OUTLET;
        if (accountType == LedgerAccountType.CUSTOMER_FUNDS) return LedgerAccountScope.CUSTOMER;
        if (accountType == LedgerAccountType.VENDOR_PAYABLE) return LedgerAccountScope.VENDOR;
        return LedgerAccountScope.BUSINESS;
    }

    private static String defaultName(LedgerAccountType type) {
        if (type == null) return null;
        return switch (type) {
            case OPERATING -> "Operating Account";
            case PAYROLL_RESERVE -> "Payroll Reserve";
            case TAX_HOLDING -> "Tax Holding";
            case ESCROW -> "Escrow";
            case SUSPENSE -> "Suspense";
            case PROVIDER_CLEARING -> "Provider Clearing";
            case TILL -> "Till";
            case SPLIT_HOLDING -> "Split Holding";
            case CUSTOMER_FUNDS -> "Customer Funds";
            case VENDOR_PAYABLE -> "Vendor Payable";
            case CUSTOM -> "Custom Account";
        };
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
