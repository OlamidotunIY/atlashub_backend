package com.atlashub.accounts.application.query.GetOrganizationDetails;

import java.time.ZonedDateTime;

public record OrganizationDetailsResult(
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
