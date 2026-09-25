package com.atlashub.accounts.application.command.SwitchActiveOrganization;

public record SwitchActiveOrganizationCommand(
        Long userId,
        Long orgId
) {
}
