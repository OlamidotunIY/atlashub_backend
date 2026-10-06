package com.atlashub.iam.application.commands.IssueApiKey;

import com.atlashub.shared.application.security.ApiEnvironment;

public record IssueApiKeyCommand(
        Long orgId,
        String name,
        ApiEnvironment environment,
        Long requestedByUserId,
        Long boundRoleId
) {}
