package com.atlashub.accounts.domain.model;

import com.atlashub.accounts.domain.valueobject.InternalAccountStatus;
import com.atlashub.accounts.domain.valueobject.InternalAccountType;
import com.atlashub.shared.domain.AggregateRoot;
import com.atlashub.shared.domain.money.CurrencyCode;
import lombok.Getter;

@Getter
public class InternalAccount extends AggregateRoot<Long> {
    private final Long id;
    private final Long organizationId;
    private final InternalAccountType type;
    private final CurrencyCode currency;
    private InternalAccountStatus status;

    public InternalAccount(Long id, Long organizationId, InternalAccountType type, CurrencyCode currency, InternalAccountStatus status) {
        this.id = id;
        this.organizationId = organizationId;
        this.type = type;
        this.currency = currency;
        this.status = status;
    }

    public static InternalAccount create(Long id, Long organizationId, InternalAccountType type, CurrencyCode currency) {
        return new InternalAccount(id, organizationId, type, currency, InternalAccountStatus.ACTIVE);
    }

    public void freeze() {
        this.status = InternalAccountStatus.FROZEN;
    }

    public void unfreeze() {
        this.status = InternalAccountStatus.ACTIVE;
    }

    public void close() {
        this.status = InternalAccountStatus.CLOSED;
    }

    @Override
    public Long getId() {
        return id;
    }
}