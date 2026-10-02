package com.atlashub.accounts.presentation.dto;

import java.time.LocalDate;
import java.time.ZonedDateTime;

public record OrganizationDetailsResponse(
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
