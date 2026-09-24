package com.atlashub.accounts.application.query.GetUserProfile;

public record OrganizationSummary(
        Long id,
        String businessName,
        String country,
        String baseCurrency,
        String logoUrl
) {
}
