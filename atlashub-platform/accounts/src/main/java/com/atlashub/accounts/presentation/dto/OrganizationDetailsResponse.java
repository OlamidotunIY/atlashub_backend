package com.atlashub.accounts.presentation.dto;

import com.atlashub.accounts.domain.valueobject.AtlasHubRegistrationType;
import com.atlashub.accounts.domain.valueobject.SupportedIndustry;

import java.time.LocalDate;
import java.time.ZonedDateTime;

public record OrganizationDetailsResponse(
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
