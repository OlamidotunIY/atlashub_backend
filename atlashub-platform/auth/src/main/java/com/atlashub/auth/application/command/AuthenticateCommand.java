package com.atlashub.auth.application.command;

public record AuthenticateCommand(
        String identifier,
        String rawCredential,
        String ipAddress,
        String userAgent
) {}
