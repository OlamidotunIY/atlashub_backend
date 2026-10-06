package com.atlashub.iam.application.commands.RecordApiKeyUsage;

public record RecordApiKeyUsageCommand(Long organizationId, String publicKey) {}
