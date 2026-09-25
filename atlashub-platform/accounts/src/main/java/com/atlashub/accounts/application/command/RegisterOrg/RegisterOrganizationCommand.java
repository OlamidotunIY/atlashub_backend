package com.atlashub.accounts.application.command.RegisterOrg;

import com.atlashub.accounts.domain.valueobject.BusinessSize;
import com.atlashub.accounts.domain.valueobject.BusinessType;
import com.atlashub.shared.domain.valueobject.Country;

public record RegisterOrganizationCommand(
        String businessName,
        BusinessType businessType,
        BusinessSize businessSize,
        String industry,
        String description,
        String logoUrl,
        String websiteUrl,
        Country country,
        String firstName,
        String lastName,
        String email,
        String password,
        Boolean isInvited
) {
}
