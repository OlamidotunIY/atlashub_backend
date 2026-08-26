package com.atlashub.identity.application.command;


public record RevokeApiKeyCommand(
    Long authenticatedOrganizationId,
    Long keyId
) {}
