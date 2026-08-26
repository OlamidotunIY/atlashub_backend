package com.atlashub.auth.application.command;

public record CompleteTwoFactorCommand(
        String preAuthToken,
        String code,
        String ipAddress,
        String userAgent
) {}
