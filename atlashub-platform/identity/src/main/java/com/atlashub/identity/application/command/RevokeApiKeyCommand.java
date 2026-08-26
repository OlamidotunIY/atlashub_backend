package com.atlashub.identity.application.command;


public record RevokeApiKeyCommand(
    Long authenticatedMerchantId,
    Long keyId
) {}
