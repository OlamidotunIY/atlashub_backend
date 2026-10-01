package com.atlashub.accounts.application.command.SwitchActiveOrganization;

import com.atlashub.accounts.domain.entities.User;

public record SwitchActiveOrganizationResult(
        User user
) {
}
