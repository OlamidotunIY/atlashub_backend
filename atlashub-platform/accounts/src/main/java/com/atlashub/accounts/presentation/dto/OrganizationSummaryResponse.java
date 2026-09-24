package com.atlashub.accounts.presentation.dto;

public record OrganizationSummaryResponse(
        Long id,
        String businessName,
        String country,
        String baseCurrency,
        String logoUrl
) {
}
