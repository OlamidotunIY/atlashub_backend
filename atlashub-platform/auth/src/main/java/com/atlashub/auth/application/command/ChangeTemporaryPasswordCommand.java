package com.atlashub.auth.application.command;

public record ChangeTemporaryPasswordCommand(
        String identifier,
        String oldPassword,
        String newPassword,
        String ipAddress,
        String userAgent
) {}
