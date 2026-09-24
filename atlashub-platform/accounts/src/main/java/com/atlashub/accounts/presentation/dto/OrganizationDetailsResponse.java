package com.atlashub.accounts.presentation.dto;

import java.time.ZonedDateTime;

public record OrganizationDetailsResponse(
        Long id,
        String businessName,
        String businessType,
        String businessSize,
        String industry,
        String description,
        String logoUrl,
        String websiteUrl,
        String country,
        String baseCurrency,
        ZonedDateTime createdAt
) {
}
