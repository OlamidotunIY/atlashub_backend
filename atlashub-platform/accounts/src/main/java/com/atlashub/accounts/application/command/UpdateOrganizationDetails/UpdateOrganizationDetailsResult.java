package com.atlashub.accounts.application.command.UpdateOrganizationDetails;

import com.atlashub.accounts.domain.model.Organization;

public record UpdateOrganizationDetailsResult(
        Organization organization
) {
}
