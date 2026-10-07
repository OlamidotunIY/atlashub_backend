package com.atlashub.accounts.application.query.GetUserProfile;

import com.atlashub.shared.application.security.ApiEnvironment;

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
        ApiEnvironment activeEnvironment,
        boolean complianceApproved,
        String activeOrganizationRole,
        ActiveOrganization activeOrganization,
        ZonedDateTime createdAt,
        List<OrganizationSummary> organizations
) {
}
