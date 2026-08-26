package com.atlashub.auth.application.command;

public record RevokeSessionCommand(
        String token
) {}
