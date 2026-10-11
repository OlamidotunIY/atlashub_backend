package com.atlashub.accounting.gl.application.query.ListAccounts;

import com.atlashub.accounting.gl.domain.valueobject.AccountType;

import java.time.ZonedDateTime;

public record AccountSummaryResult(
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
