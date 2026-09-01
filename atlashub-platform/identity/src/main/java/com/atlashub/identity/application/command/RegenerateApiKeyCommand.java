package com.atlashub.identity.application.command;

import com.atlashub.identity.domain.valueobject.ApiEnvironment;
import com.atlashub.identity.domain.valueobject.KeyType;

public record RegenerateApiKeyCommand(
    Long authenticatedOrganizationId,
    KeyType keyType,
    ApiEnvironment environment
) {}

