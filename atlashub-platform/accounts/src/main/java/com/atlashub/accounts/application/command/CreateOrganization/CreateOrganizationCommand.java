package com.atlashub.accounts.application.command.CreateOrganization;

import com.atlashub.accounts.domain.valueobject.AtlasHubRegistrationType;
import com.atlashub.accounts.domain.valueobject.SupportedIndustry;
import com.atlashub.shared.domain.valueobject.Country;
import com.atlashub.shared.domain.valueobject.CurrencyCode;

public record CreateOrganizationCommand(Long ownerUserId, String businessName,
        AtlasHubRegistrationType registrationType, SupportedIndustry industry,
        String description, String logoUrl, String websiteUrl, Country country,
        CurrencyCode baseCurrency) {}
