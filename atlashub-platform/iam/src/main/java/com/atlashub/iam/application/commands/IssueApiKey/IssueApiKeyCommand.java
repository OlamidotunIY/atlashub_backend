package com.atlashub.iam.application.commands.IssueApiKey;

public record IssueApiKeyCommand(Long orgId, String name, String environment, Long requestedByUserId) {}
