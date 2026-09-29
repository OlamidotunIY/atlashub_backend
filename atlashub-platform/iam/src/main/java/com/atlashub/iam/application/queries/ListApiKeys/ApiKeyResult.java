package com.atlashub.iam.application.queries.ListApiKeys;

import java.time.ZonedDateTime;

public record ApiKeyResult(
    Long id,
    Long organizationId,
    String publicKey,
    String name,
    String environment,
    Boolean isRevoked,
    ZonedDateTime lastUsedAt,
    ZonedDateTime createdAt
) {}
