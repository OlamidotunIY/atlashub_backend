package com.atlashub.accounting.gl.application.command.CreateAccount;

import com.atlashub.accounting.gl.domain.valueobject.AccountType;

public record CreateAccountCommand(
        Long organizationId,
        String code,
        String name,
        AccountType type,
        Long parentAccountId,
        boolean isSystemAccount
) {}
