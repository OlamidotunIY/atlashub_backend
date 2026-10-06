package com.atlashub.accounts.presentation.dto;

import java.time.LocalDate;
import java.time.ZonedDateTime;

public record OrganizationDetailsResponse(
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
