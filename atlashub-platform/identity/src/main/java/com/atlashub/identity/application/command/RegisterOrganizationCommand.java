package com.atlashub.identity.application.command;

import com.atlashub.identity.domain.valueobject.BusinessType;

public record RegisterOrganizationCommand(
    Long userId,
    String businessName,
    BusinessType businessType
) {}
