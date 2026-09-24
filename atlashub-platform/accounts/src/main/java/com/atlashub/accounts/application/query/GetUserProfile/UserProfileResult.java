package com.atlashub.accounts.application.query.GetUserProfile;

import java.time.ZonedDateTime;
import java.util.List;

public record UserProfileResult(
        Long id,
        String firstName,
        String lastName,
        String email,
        String phone,
        String imageUrl,
        String country,
        Long activeOrganizationId,
        ZonedDateTime createdAt,
        List<OrganizationSummary> organizations
) {
}
