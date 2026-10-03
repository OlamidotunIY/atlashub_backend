package com.atlashub.accounts.application.query.GetOrganizationDetails;

import java.time.LocalDate;
import java.time.ZonedDateTime;

public record OrganizationDetailsResult(
        Long id,
        String businessName,
        String registrationType,
        String industry,
        LocalDate registrationDate,
        String description,
        String logoUrl,
        String websiteUrl,
        String country,
        String baseCurrency,
        ZonedDateTime createdAt
) {
}
