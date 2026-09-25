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
        Long activeOrganizationId,
        ZonedDateTime createdAt,
        List<OrganizationSummaryResponse> organizations
) {
}
