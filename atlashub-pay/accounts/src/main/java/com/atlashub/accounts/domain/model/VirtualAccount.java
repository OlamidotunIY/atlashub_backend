package com.atlashub.accounts.domain.model;

import com.atlashub.shared.domain.AggregateRoot;
import com.atlashub.shared.domain.valueobject.NUBAN;
import com.atlashub.shared.domain.exception.BusinessRuleException;
import com.atlashub.accounts.domain.exception.AccountsErrorCode;
import com.atlashub.accounts.domain.valueobject.AccountStatus;
import com.atlashub.accounts.domain.event.*;
import com.atlashub.shared.domain.money.CurrencyCode;
import lombok.Getter;

import java.time.ZonedDateTime;
import java.util.UUID;

@Getter
public class VirtualAccount extends AggregateRoot<Long> {

    private final Long id;
    private final Long integration;
    private final String UserCode;
    private final String accountName;
    private final String bankName;
    private final String idempotencyKey;
    private final CurrencyCode currency;
    private AccountStatus status;
    private NUBAN nuban;

    public VirtualAccount(Long id, Long integration, String UserCode, String accountName, String bankName, String idempotencyKey, CurrencyCode currency, AccountStatus status, NUBAN nuban) {
        this.id = id;
        this.integration = integration;
        this.UserCode = UserCode;
        this.accountName = accountName;
        this.bankName = bankName;
        this.idempotencyKey = idempotencyKey;
        this.currency = currency;
        this.status = status;
        this.nuban = nuban;
    }

    public static VirtualAccount create(Long id, Long integration, String UserCode, String accountName, String bankName, String idempotencyKey, CurrencyCode currency) {
        VirtualAccount account = new VirtualAccount(id, integration, UserCode, accountName, bankName, idempotencyKey, currency, AccountStatus.PENDING_ISSUANCE, null);
        account.registerEvent(new VirtualAccountCreatedEvent(
                UUID.randomUUID().toString(),
                String.valueOf(id),
                ZonedDateTime.now(),
                null
        ));
        return account;
    }

    public void activate(NUBAN nuban) {
        if (this.status != AccountStatus.PENDING_ISSUANCE) {
            throw new BusinessRuleException(AccountsErrorCode.INVALID_ACCOUNT_STATE, "Account must be in PENDING_ISSUANCE state to assign a NUBAN");
        }
        if (nuban == null) {
            throw new IllegalArgumentException("NUBAN cannot be null");
        }
        
        this.nuban = nuban;
        this.status = AccountStatus.ACTIVE;
        registerEvent(new VirtualAccountActivatedEvent(
                UUID.randomUUID().toString(),
                String.valueOf(this.id),
                ZonedDateTime.now(),
                new VirtualAccountActivatedEvent.Payload(this.integration, nuban.value())
        ));
    }

    public void requestClosure() {
        if (this.status == AccountStatus.CLOSED || this.status == AccountStatus.CLOSURE_REQUESTED) {
            throw new BusinessRuleException(AccountsErrorCode.INVALID_ACCOUNT_STATE, "Account is already closed or closure is requested");
        }
        this.status = AccountStatus.CLOSURE_REQUESTED;
        registerEvent(new VirtualAccountClosureRequestedEvent(
                UUID.randomUUID().toString(),
                String.valueOf(id),
                ZonedDateTime.now(),
                null
        ));
    }

    public void close() {
        if (this.status == AccountStatus.CLOSED) {
            throw new BusinessRuleException(AccountsErrorCode.INVALID_ACCOUNT_STATE, "Account is already closed");
        }
        this.status = AccountStatus.CLOSED;
        registerEvent(new VirtualAccountClosedEvent(
                UUID.randomUUID().toString(),
                String.valueOf(id),
                ZonedDateTime.now(),
                null
        ));
    }

    @Override
    public Long getId() {
        return id;
    }
}
