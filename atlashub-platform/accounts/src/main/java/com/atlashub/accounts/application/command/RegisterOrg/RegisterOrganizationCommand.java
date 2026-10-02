package com.atlashub.accounts.application.command.RegisterOrg;

import com.atlashub.accounts.domain.valueobject.AtlasHubRegistrationType;
import com.atlashub.accounts.domain.valueobject.SupportedIndustry;
import com.atlashub.shared.domain.valueobject.Country;

public record RegisterOrganizationCommand(
        String businessName,
        AtlasHubRegistrationType registrationType,
        SupportedIndustry industry,
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
