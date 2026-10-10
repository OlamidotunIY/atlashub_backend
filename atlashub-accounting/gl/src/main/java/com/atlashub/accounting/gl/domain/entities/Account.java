package com.atlashub.accounting.gl.domain.entities;

import com.atlashub.accounting.gl.domain.exceptions.InvalidEntryStateException;
import com.atlashub.accounting.gl.domain.exceptions.SystemAccountImmutableException;
import com.atlashub.accounting.gl.domain.valueobject.AccountType;
import com.atlashub.shared.domain.entities.AggregateRoot;
import lombok.Getter;

import java.time.ZonedDateTime;
import java.util.Objects;

@Getter
public class Account extends AggregateRoot<Long> {

    private final Long id;
    private final Long organizationId;
    private final String code;
    private final String name;
    private final AccountType type;
    private final Long parentAccountId;
    private final Boolean isSystemAccount;
    private Boolean isActive;
    private final ZonedDateTime createdAt;

    public Account(Long id,
                   Long organizationId,
                   String code,
                   String name,
                   AccountType type,
                   Long parentAccountId,
                   Boolean isSystemAccount,
                   Boolean isActive,
                   ZonedDateTime createdAt) {
        this.id = id;
        this.organizationId = organizationId;
        this.code = code;
        this.name = name;
        this.type = type;
        this.parentAccountId = parentAccountId;
        this.isSystemAccount = isSystemAccount;
        this.isActive = isActive;
        this.createdAt = createdAt;
    }

    public static Account create(Long id,
                                 Long organizationId,
                                 String code,
                                 String name,
                                 AccountType type,
                                 Long parentAccountId,
                                 boolean isSystemAccount) {
        Objects.requireNonNull(id, "Account ID must not be null");
        Objects.requireNonNull(organizationId, "Organization ID must not be null");
        Objects.requireNonNull(type, "AccountType must not be null");
        if (code == null || code.isBlank()) {
            throw new InvalidEntryStateException("Account code must not be blank");
        }
        if (name == null || name.isBlank()) {
            throw new InvalidEntryStateException("Account name must not be blank");
        }

        return new Account(
                id,
                organizationId,
                code.trim(),
                name.trim(),
                type,
                parentAccountId,
                isSystemAccount,
                true,
                ZonedDateTime.now()
        );
    }

    public void deactivate() {
        if (Boolean.TRUE.equals(isSystemAccount)) {
            throw new SystemAccountImmutableException();
        }
        this.isActive = false;
    }

    public void activate() {
        this.isActive = true;
    }

    @Override
    public Long getId() {
        return id;
    }
}
