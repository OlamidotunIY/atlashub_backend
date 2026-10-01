package com.atlashub.accounts.application.command.RegisterOrg;

import com.atlashub.accounts.domain.entities.Organization;
import com.atlashub.accounts.domain.entities.User;

public record RegisterOrganizationResult(
    Organization organization,
    User user
) {}
