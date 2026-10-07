package com.atlashub.accounts.application.query.GetUserProfile;

import com.atlashub.accounts.domain.valueobject.AtlasHubRegistrationType;
import com.atlashub.accounts.domain.valueobject.SupportedIndustry;

import java.time.LocalDate;
import java.time.ZonedDateTime;

public record ActiveOrganization(
        Long id,
        String businessName,
        AtlasHubRegistrationType registrationType,
        SupportedIndustry industry,
        LocalDate registrationDate,
        String description,
        String logoUrl,
        String websiteUrl,
        String country,
        String baseCurrency,
        ZonedDateTime createdAt
) {
}
