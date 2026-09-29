package com.atlashub.iam.application.commands.RevokeApiKey;

public record RevokeApiKeyCommand(Long keyId, Long orgId, Long requestedByUserId) {}
