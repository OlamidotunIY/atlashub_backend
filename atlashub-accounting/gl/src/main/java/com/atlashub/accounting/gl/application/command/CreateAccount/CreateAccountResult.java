package com.atlashub.accounting.gl.application.command.CreateAccount;

import com.atlashub.accounting.gl.domain.valueobject.AccountType;

import java.time.ZonedDateTime;

public record CreateAccountResult(
        Long id,
        Long organizationId,
        String code,
        String name,
        AccountType type,
        Long parentAccountId,
        Boolean isSystemAccount,
        Boolean isActive,
        ZonedDateTime createdAt
) {}
