package com.atlashub.accounts.presentation.dto;

import java.time.ZonedDateTime;
import java.util.List;

public record UserProfileResponse(
        Long id,
        String firstName,
        String lastName,
        String email,
        String phone,
        String imageUrl,
        String country,
        String activeEnvironment,
        String activeOrganizationRole,
        OrganizationDetailsResponse activeOrganization,
        ZonedDateTime createdAt,
        List<OrganizationSummaryResponse> organizations
) {
}
