package com.atlashub.auth.application.command;

public record SetupPasswordCommand(
    String setupToken,
    String newPassword,
    String ipAddress,
    String userAgent
) {}