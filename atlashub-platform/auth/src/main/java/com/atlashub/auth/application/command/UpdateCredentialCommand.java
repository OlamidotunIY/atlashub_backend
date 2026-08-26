package com.atlashub.auth.application.command;

public record UpdateCredentialCommand(
        Long authAccountId,
        String rawNewCredential
) {}
