package com.atlashub.authentication.application.command.SwitchOrganization;

public record SwitchOrganizationCommand(Long userId, Long currentSessionId, Long targetOrganizationId) {
}
