package com.atlashub.authentication.application.command.SwitchEnvironment;

import com.atlashub.shared.application.security.ApiEnvironment;

public record SwitchEnvironmentCommand(Long userId, Long currentSessionId, ApiEnvironment targetEnvironment) {
}
