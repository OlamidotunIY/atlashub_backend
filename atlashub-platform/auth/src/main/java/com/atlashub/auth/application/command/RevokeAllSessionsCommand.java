package com.atlashub.auth.application.command;

public record RevokeAllSessionsCommand(
        Long authAccountId
) {}
