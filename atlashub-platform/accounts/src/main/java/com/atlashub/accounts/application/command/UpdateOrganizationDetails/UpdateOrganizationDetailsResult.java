package com.atlashub.accounts.application.command.UpdateOrganizationDetails;

import com.atlashub.accounts.domain.entities.Organization;

public record UpdateOrganizationDetailsResult(
        Organization organization
) {
}
