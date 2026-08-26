package com.atlashub.auth.application.command;

public record EnableTotpCommand(
        Long authAccountId
) {}
