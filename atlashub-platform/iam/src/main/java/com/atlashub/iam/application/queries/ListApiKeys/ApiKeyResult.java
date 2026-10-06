package com.atlashub.iam.application.queries.ListApiKeys;

import com.atlashub.shared.application.security.ApiEnvironment;

import java.time.ZonedDateTime;

public record ApiKeyResult(
    Long id,
    Long organizationId,
    String publicKey,
    String name,
    ApiEnvironment environment,
    Boolean isRevoked,
    Long boundRoleId,
    ZonedDateTime lastUsedAt,
    ZonedDateTime createdAt
) {}
