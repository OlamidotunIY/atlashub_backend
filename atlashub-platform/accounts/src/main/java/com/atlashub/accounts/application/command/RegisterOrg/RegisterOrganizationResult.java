package com.atlashub.accounts.application.command.RegisterOrg;

import com.atlashub.accounts.domain.model.Organization;
import com.atlashub.accounts.domain.model.User;

public record RegisterOrganizationResult(
    Organization organization,
    User user
) {}
