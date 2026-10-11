package com.atlashub.accounting.gl.presentation.dto;

import com.atlashub.accounting.gl.domain.valueobject.AccountType;

import java.time.ZonedDateTime;

public record AccountResponse(
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
