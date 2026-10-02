package com.atlashub.accounts.application.query.GetOrganizationDetails;

import java.time.LocalDate;
import java.time.ZonedDateTime;

public record OrganizationDetailsResult(
        Long id,
        String businessName,
        String registrationType,
        String industry,
        LocalDate legalRegistrationDate,
        String businessRegistrationNumber,
        String description,
        String logoUrl,
        String websiteUrl,
        String country,
        String baseCurrency,
        ZonedDateTime createdAt
) {
}
