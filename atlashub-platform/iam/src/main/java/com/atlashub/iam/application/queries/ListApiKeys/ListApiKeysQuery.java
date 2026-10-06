package com.atlashub.iam.application.queries.ListApiKeys;

import com.atlashub.shared.application.security.ApiEnvironment;

public record ListApiKeysQuery(Long orgId, ApiEnvironment environment) {}
