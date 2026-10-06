package com.atlashub.accounts.application.command.UpdateOrganizationDetails;

import com.atlashub.accounts.domain.valueobject.SupportedIndustry;

public record UpdateOrganizationDetailsCommand(
        Long organizationId,
        String businessName,
        String description,
        String logoUrl,
        SupportedIndustry industry,
        String websiteUrl
) {
}
