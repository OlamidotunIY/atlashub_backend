package com.atlashub.accounts.application.command.SwitchActiveOrganization;

import com.atlashub.accounts.domain.model.User;

public record SwitchActiveOrganizationResult(
        User user
) {
}
