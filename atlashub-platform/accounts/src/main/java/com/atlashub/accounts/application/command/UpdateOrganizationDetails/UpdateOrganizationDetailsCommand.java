package com.atlashub.accounts.application.command.UpdateOrganizationDetails;

public record UpdateOrganizationDetailsCommand(
        Long organizationId,
        String businessName,
        String description,
        String logoUrl,
        String industry,
        String websiteUrl
) {
}
