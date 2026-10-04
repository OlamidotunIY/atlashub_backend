package com.atlashub.iam.application.commands.RotateApiKey;

public record RotateApiKeyCommand(Long keyId, Long organizationId, Long requestedByUserId, String name) {}
