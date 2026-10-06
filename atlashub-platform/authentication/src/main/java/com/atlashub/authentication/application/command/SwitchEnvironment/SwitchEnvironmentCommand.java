package com.atlashub.authentication.application.command.SwitchEnvironment;

public record SwitchEnvironmentCommand(Long userId, Long currentSessionId, String targetEnvironment) {
}
